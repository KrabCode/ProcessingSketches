import applet.HotswapGuiSketch;
import processing.core.PVector;

import java.util.ArrayList;

@SuppressWarnings({"DuplicatedCode", "unused"})
public class Boids extends HotswapGuiSketch {

    //TODO
    // - efficient division, compute shaders?
    // - food chain
    // - player takes control of eater when eaten
    // - player gets bigger as he eats

    private static final float INITIAL_DISTANCE_VALUE = -1;
    private ArrayList<Boid> boids = new ArrayList<Boid>();
    private ArrayList<Boid> toRemove = new ArrayList<Boid>();
    private boolean targetActive;
    private float avoidRadius, avoidMag, centerRadius, centerMag, alignRadius, alignMag, obstacleRadius, obstacleMag, obstacleAngle;
    private float farAway, centerToCornerDistance;
    private Boid player;
    private PVector cameraOffset, playerTarget;
    private boolean debug;
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("Boids");
    }

    public void settings() {
        size(800, 800, P2D);
//        fullScreen(P2D);
        recordingFrames *= 4;
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        cameraOffset = new PVector(width * .5f, height * .5f);
        centerToCornerDistance = dist(0, 0, width * .5f, height * .5f);
        farAway = centerToCornerDistance * 2;
        playerTarget = new PVector();

        player = new Boid();
        player.pos = new PVector();
        playerTarget = randomPositionOffscreenInFrontOfPlayer();
        player.isPlayer = true;
        boids.add(player);
    }

    public void draw() {
        t += radians(slider("t", 4));
        sliders();
        bg();
        updateCamera();
        updateBoids();
        rec();
        gui();
    }

    private void sliders() {
        debug = toggle("debug", false);
        avoidRadius = slider("avoid r", 40);
        avoidMag = slider("avoid mag", .4f);
        obstacleRadius = slider("obst r", 80);
        obstacleMag = slider("obst mag", 0, .3f);
        obstacleAngle = slider("obst angle", PI);
        centerRadius = slider("center r", 180);
        centerMag = slider("center mag", .01f);
        alignRadius = slider("align r", 160);
        alignMag = slider("align mag", .24f);
    }

    private void bg() {
        background(0);
    }

    private void updateCamera() {
        Boid player = getPlayer();
        float cameraTightness = .05f; // slider("cam tight", .3f);
        cameraOffset.x = lerp(cameraOffset.x, width * .5f - player.pos.x, cameraTightness);
        cameraOffset.y = lerp(cameraOffset.y, height * .5f - player.pos.y, cameraTightness);
        translate(cameraOffset.x, cameraOffset.y);
    }

    private Boid getPlayer() {
        for (Boid b : boids) {
            if (b.isPlayer) {
                return b;
            }
        }
        throw new IllegalStateException("No player found in boids list");
    }

    private void updateBoids() {
        player = getPlayer();
        removeDeadBoids();
        updateBoidCount();
        for (Boid boid : boids) {
            reactToOtherBoids(boid);
            if (boid.isPlayer) {
                updatePlayerTarget();
                updatePlayer();
            } else {
                float distanceToPlayer = dist(player.pos.x, player.pos.y, boid.pos.x, boid.pos.y);
                if (isBehindPlayer(boid) && distanceToPlayer > farAway) {
//                    println("boid died because he was far behind the player: ", boid.pos, " - ", player.pos);
                    boid.dead = true;
                }
            }
            displayBoid(boid);
            moveBoid(boid);
        }
    }

    private void moveBoid(Boid boid) {
        boid.spd.add(boid.acc);
        float angularDifferenceFromLastSpd = angleDifference(boid.lastSpd.heading(), boid.spd.heading());
        float maximumAllowedAngularDifference = radians(slider("max rotation", 6));
        if(abs(angularDifferenceFromLastSpd) > maximumAllowedAngularDifference){
            boid.spd.rotate(-angularDifferenceFromLastSpd);
            if(angularDifferenceFromLastSpd > 0){
                boid.spd.rotate(maximumAllowedAngularDifference);
            }else{
                boid.spd.rotate(-maximumAllowedAngularDifference);
            }
        }
        float minMag = slider("minimum speed", 5);
        if (boid.spd.mag() < minMag) {
            if (boid.spd.mag() == 0) {
                boid.spd.add(PVector.random2D());
            }
            boid.spd.setMag(minMag);
        }
        boid.spd.mult(slider("drag", .5f, 1));
        boid.pos.add(boid.spd);
        boid.acc.mult(0);

        boid.lastSpd.x = boid.spd.x;
        boid.lastSpd.y = boid.spd.y;
    }


    private void reactToOtherBoids(Boid me) {
        //first we find the average location of the birds affecting every behavior
        PVector towardsCenterSum = new PVector();
        PVector alignHeadingSum = new PVector();
        PVector obstacleSum = new PVector();
        int towardsCenterCount = 0, alignHeadingCount = 0, obstacleCount = 0;

        for (Boid other : boids) {
            if (me.equals(other)) {
                continue;
            }
            //avoid boids that are too close
            float d = INITIAL_DISTANCE_VALUE;
            PVector avoid = new PVector();
            if (isBoidInRectangle(me, other, avoidRadius)) {
                d = dist(me.pos.x, me.pos.y, other.pos.x, other.pos.y);
                if (d < avoidRadius) {
                    PVector thisAvoidance = PVector.sub(me.pos, other.pos);
                    avoid.add(thisAvoidance.normalize().mult(1 / thisAvoidance.mag()).mult(avoidMag));
                    if (me.equals(player)) {
                        stroke(255, 100, 100);
                        displayDebugCircle(me, avoidRadius);
                    }
                }
            }
            me.acc.add(avoid);

            stroke(255);
            // the boid moves towards others inside center radius
            if (isBoidInRectangle(me, other, centerRadius)) {
                d = lazyDistanceEval(me, other, d);
                if (d < centerRadius) {
                    towardsCenterSum.add(PVector.sub(other.pos, me.pos));
                    towardsCenterCount++;
                    if (me.equals(player)) {
                        stroke(100, 255, 100);
                        displayDebugCircle(me, centerRadius);
                    }
                }
            }

            // the boid aligns its direction with others within align range
            if (isBoidInRectangle(me, other, alignRadius)) {
                d = lazyDistanceEval(me, other, d);
                if (d < alignRadius) {
                    alignHeadingSum.add(other.spd);
                    alignHeadingCount++;
                    if (me.equals(player)) {
                        stroke(255, 100, 255);
                        displayDebugCircle(me, alignRadius);
                    }
                }
            }

            // find obstructing boids in a pie shaped perimeter in front of the bird
            if (isBoidInRectangle(me, other, obstacleRadius)) {
                d = lazyDistanceEval(me, other, d);
                if (d < obstacleRadius) {
                    float angleDifference = angleDifferenceToMyHeading(me, other.pos);
                    if (abs(angleDifference) > PI - obstacleAngle * .5f) {
                        obstacleSum.add(other.pos);
                        obstacleCount++;
                    }
                }

            }
        }


        //dividing by zero is very dangerous
        if (towardsCenterCount > 0) {
            PVector avg = towardsCenterSum.div(towardsCenterCount);
            me.acc.add(avg.normalize().mult(centerMag));
        }
        if (alignHeadingCount > 0) {
            PVector avg = alignHeadingSum.div(alignHeadingCount);
            float desiredAngle = avg.heading();
            float towardsDesiredAngle = angleDifference(me.spd.heading(), desiredAngle);
            float myRotation = lerp(0, towardsDesiredAngle, radians(alignMag));
            me.spd.rotate(myRotation);
        }
        if (obstacleCount > 0) {
            PVector avg = obstacleSum.div(obstacleCount);
            float differenceToMyHeading = angleDifferenceToMyHeading(me, avg);
            float differenceToMyHeadingAbsNorm = norm(abs(differenceToMyHeading), 0, HALF_PI);
            float towardsDesiredAngle = differenceToMyHeading > 0 ? - QUARTER_PI : QUARTER_PI;
            float myRotation = lerp(0, towardsDesiredAngle, (1/differenceToMyHeadingAbsNorm)*obstacleMag);
            me.spd.rotate(myRotation);

            if(me.isPlayer){
                stroke(255, 0, 0);
                displayDebugDirection(me.pos, me.spd.heading()+towardsDesiredAngle);
            }
        }
    }

    private float angleDifferenceToMyHeading(Boid me, PVector other) {
        float heading = me.spd.heading();
        float angleBetween = angleToB(other, me.pos);
        return angleDifference(angleBetween, heading);
    }

    private void displayDebugLine(PVector a, PVector b) {
        if (!debug) {
            return;
        }
        line(a.x, a.y, b.x, b.y);
    }

    private void displayDebugDirection(PVector origin, float dir) {
        if (!debug) {
            return;
        }
        pushMatrix();
        translate(origin.x, origin.y);
        rotate(dir);
        line(0, 0, 20, 0);
        popMatrix();
    }

    private void displayDebugCircle(Boid boid, float radius) {
        if (!debug) {
            return;
        }
        noFill();
        ellipseMode(CENTER);
        ellipse(boid.pos.x, boid.pos.y, radius * 2, radius * 2);
    }

    private void displayDebugArc(Boid boid, float radius, float angularSize) {
        if (!debug) {
            return;
        }
        pushMatrix();
        translate(boid.pos.x, boid.pos.y);
        rotate(boid.spd.heading());
        noFill();
        ellipseMode(CENTER);
        arc(0, 0, radius * 2, radius * 2, -angularSize * .5f, angularSize * .5f, PIE);
//        line(0, 0, radius*cos(-angularSize*.5f), radius*sin(-angularSize*.5f));
//        line(0, 0, radius*cos(angularSize*.5f), radius*sin(angularSize*.5f));
        popMatrix();
    }

    private float lazyDistanceEval(Boid a, Boid b, float d) {
        if (d == INITIAL_DISTANCE_VALUE) {
            d = dist(a.pos.x, a.pos.y, b.pos.x, b.pos.y);
        }
        return d;
    }

    private void updatePlayer() {
        float mouseTightness = slider("mouse tight", 3);
        if (targetActive) {
            PVector towardsTarget = PVector.sub(playerTarget, player.pos);
            player.acc.add(towardsTarget.normalize().mult(mouseTightness));
        }

    }

    private void updatePlayerTarget() {
        if (mousePressedOutsideGui) {
            playerTarget.x = mouseX - cameraOffset.x;
            playerTarget.y = mouseY - cameraOffset.y;
            targetActive = true;
        }
        if (!mousePressedOutsideGui || dist(player.pos.x, player.pos.y, playerTarget.x, playerTarget.y) < 5) {
            targetActive = false;
        }
    }

    private void updateBoidCount() {
        int intendedBoidCount = floor(slider("count", 1, 201));
        while (boids.size() < intendedBoidCount) {
            boids.add(new Boid(randomPositionOffscreenInFrontOfPlayer()));
        }
        while (boids.size() > intendedBoidCount) {
            boids.remove(boids.size() - 1);
        }
    }

    private void removeDeadBoids() {
        toRemove.clear();
        for (Boid b : boids) {
            if (b.dead) {
                toRemove.add(b);
            }
        }
        boids.removeAll(toRemove);
    }

    private boolean isBoidInRectangle(Boid a, Boid b, float range) {
        return isPointInRect(a.pos.x, a.pos.y,
                b.pos.x - range,
                b.pos.y - range,
                range * 2,
                range * 2);
    }

    private boolean isBehindPlayer(Boid who) {
        return abs(normalizedAngleToPlayerHeading(who)) < HALF_PI;
    }


    private float normalizedAngleToPlayerHeading(Boid who) {
        float angleToPlayer = angleToPlayer(who);
        float normalizedAngleToPlayer = normalizeAngle(angleToPlayer, PI);
        float normalizedPlayerHeading = normalizeAngle(player.spd.heading(), PI);
        return normalizeAngle(normalizedAngleToPlayer - normalizedPlayerHeading, 0);
    }

    private float angleToPlayer(Boid who) {
        return angleToB(who, player);
    }

    private float angleToB(Boid a, Boid b) {
        return angleToB(a.pos, b.pos);
    }

    private float angleToB(PVector a, PVector b) {
        return atan2(b.y - a.y, b.x - a.x);
    }

    private float normalizeAngle(float a, float center) {
        return a - TWO_PI * floor((a + PI - center) / TWO_PI);
    }

    private float angleDifference(float angleStart, float angleTarget) {
        float d = angleTarget - angleStart;
        while (d > PI) {
            d -= TWO_PI;
        }
        while (d < -PI) {
            d += TWO_PI;
        }
        return d;
    }

    private PVector randomPositionOffscreenInFrontOfPlayer() {
        float angle = random(player.spd.heading() - HALF_PI, player.spd.heading() + HALF_PI);
        float distance = random(centerToCornerDistance, farAway);
        return new PVector(player.pos.x + distance * cos(angle), player.pos.y + distance * sin(angle));
    }

    private class Boid {
        float bodyLength = 10 + randomGaussian();
        float bodyWidth = bodyLength * .15f;
        float wingWidth = bodyLength * 2.5f;
        float wingHeight = bodyLength * .35f;
        float tailRadius = bodyLength * .5f;
        float tailAngle = .8f;

        PVector pos = new PVector();
        PVector spd = new PVector();
        PVector acc = new PVector();
        PVector lastSpd = new PVector();
        boolean dead = false;
        boolean isPlayer = false;

        Boid() {

        }

        Boid(PVector spawnPos) {
            pos = spawnPos;
        }
    }


    private void displayBoid(Boid boid) {
        if (boid.equals(player)) {
            stroke(40, 40, 200);
            displayDebugArc(boid, obstacleRadius, obstacleAngle);
        }
        pushMatrix();
        translate(boid.pos.x, boid.pos.y);
        scale(slider("scl", 3.2f));
        rotate(boid.spd.heading());
        fill(255);
        noStroke();
        strokeWeight(1);
        ellipseMode(CENTER);
        fill(255);
        body(boid.bodyLength, boid.bodyWidth);
        translate(-boid.bodyLength * .05f, 0);
        tail(boid.tailRadius, boid.tailAngle);
        wing(boid.wingWidth, boid.wingHeight, false);
        wing(boid.wingWidth, boid.wingHeight, true);
        popMatrix();
    }


    private void body(float bodyLength, float bodyWidth) {
        ellipse(0, 0, bodyLength, bodyWidth);
    }

    private void tail(float tailRadius, float tailAngle) {
        int res = 7;
        beginShape(TRIANGLE_STRIP);
        for (int i = 0; i < res; i++) {
            float inorm = norm(i, 0, res - 1);
            float angle = map(inorm, 0, 1, PI - tailAngle * .5f, PI + tailAngle * .5f);
            float x = tailRadius * cos(angle);
            float y = tailRadius * sin(angle);
            fill(255);
            vertex(0, 0);
            fill(100);
            vertex(x, y);
        }
        endShape();
    }

    private void wing(float wingSpan, float wingHeight, boolean right) {
        float wingSign = right ? -1 : 1;
        beginShape(TRIANGLE_STRIP);
        int res = 7;
        for (int i = 0; i < res; i++) {
            float inorm = norm(i, 0, res - 1);
            float oddSign = i % 2 == 0 ? -1 : 1;
            float taper = constrain(inorm > .5f ? 2 - inorm * 1.8f : 1, 0, 1);
            float flap = inorm * sin(inorm * 5 - t * 5);
            noStroke();
            if (i == 0) {
                fill(255);
                vertex(wingHeight * .5f * taper, wingSign * wingSpan * .5f * inorm);
            }
            if (oddSign > 0) {
                fill(255);
            } else {
                fill(0);
            }
            vertex(oddSign * wingHeight * .5f * taper + flap * wingHeight * .5f,
                    wingSign * wingSpan * .5f * inorm);
        }
        endShape();
    }
}
