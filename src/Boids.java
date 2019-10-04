import applet.HotswapGuiSketch;
import processing.core.PVector;

import java.util.ArrayList;

public class Boids extends HotswapGuiSketch {

    //TODO
    // - pretty birds
    // - pretty fish

    // - efficient division, compute shaders?
    // - camera moving with player
    // - food chain
    // - player takes control of eater when eaten
    // - player gets bigger as he eats

    private static final float INITIAL_DISTANCE_VALUE = -1;
    ArrayList<Boid> boids = new ArrayList<Boid>();
    ArrayList<Boid> toRemove = new ArrayList<Boid>();
    boolean targetActive;
    float avoidRadius, avoidMag, centerRadius, centerMag, alignRadius, alignMag, obstructionRadius, obstructionMag, obstructionViewportAngle;
    float farAway, centerToCornerDistance;
    private Boid player;
    private PVector cameraOffset, playerTarget;
    private boolean debug;
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("Boids");
    }

    public void settings() {
        size(800, 800, P2D);
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
        t += radians(slider("t", 10));
        sliders();
        bg();
        updateCamera();
        updateBoids();
        rec();
        gui();
    }

    private void sliders() {
        debug = toggle("debug", true);
        avoidRadius = slider("avoid r", 40);
        avoidMag = slider("avoid mag", 0, 1, .8f);

        obstructionRadius = slider("obst r", 80);
        obstructionMag = slider("obst mag", 0, 1, .6f);
        obstructionViewportAngle = slider("obst angle", TWO_PI);

        centerRadius = slider("center r", 180);
        centerMag = slider("center mag", .5f);

        alignRadius = slider("align r", 100);
        alignMag = slider("align mag", .5f);

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
//                    println("boid died because ", boid.pos, " - ", player.pos);
                    boid.dead = true;
                }
            }
            displayBoid(boid);
            moveBoid(boid);
        }
    }

    private void displayBoid(Boid boid) {
        if (boid.equals(player)) {
            stroke(40, 40, 200);
            displayDebugArc(boid, obstructionRadius, obstructionViewportAngle);
        }
        pushMatrix();
        translate(boid.pos.x, boid.pos.y);
        rotate(boid.spd.heading());
        fill(255);
        stroke(255);
        strokeWeight(1);
        ellipseMode(CENTER);
        ellipse(0, 0, boid.bodyLength, boid.bodyWidth);
        line(0, 0, boid.bodyLength * .5f + boid.beakLength, 0);
        ellipse(-boid.bodyLength * .5f, 0, boid.tailSize, boid.tailSize);
        beginShape(TRIANGLE_STRIP);
        int i = 0;
        for (float y = -boid.wingWidth; y < boid.wingWidth; y ++) {
            i++;
            float sign = i % 2 == 0 ? -1 : 1;
            float x = sign * boid.wingHeight * .5f + .5f *  abs(boid.wingHeight*sin(abs(y)*slider("wing freq")-t));
            vertex(x, y);
        }
        endShape();
        popMatrix();
    }

    private void moveBoid(Boid boid) {
        boid.spd.add(boid.acc);
        float minMag = slider("minMag", 5);
        if (boid.spd.mag() < minMag) {
            if (boid.spd.mag() == 0) {
                boid.spd.add(PVector.random2D());
            }
            boid.spd.setMag(minMag);
        }
        boid.spd.mult(slider("drag", .5f, 1));
        boid.pos.add(boid.spd);
        boid.acc.mult(0);
    }


    private void reactToOtherBoids(Boid me) {
        //first we find the average location of the birds affecting every behavior
        PVector towardsCenterSum = new PVector();
        PVector alignHeadingSum = new PVector();
        PVector obstructionSum = new PVector();
        int towardsCenterCount = 0, alignHeadingCount = 0, obstructionCount = 0;

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

            // the boid is not avoiding so it can do something else
            if (avoid.mag() < .01f) {
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
                if (isBoidInRectangle(me, other, obstructionRadius)) {
                    d = lazyDistanceEval(me, other, d);
                    if (d < obstructionRadius) {
                        float angleDifference = angleDifferenceToMyHeading(me, other.pos);
                        if (abs(angleDifference) > PI - obstructionViewportAngle * .5f) {
                            obstructionSum.add(other.pos);
                            obstructionCount++;
                        }
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
            //TODO only allow  a little bit of rotation at a time
            me.spd.rotate(constrain(towardsDesiredAngle, radians(-alignMag), radians(alignMag)));
        }
        if (obstructionCount > 0) {
            PVector avg = obstructionSum.div(obstructionCount);
            float angleBetween = angleDifferenceToMyHeading(me, avg);
            boolean obstructedFromTheRight = angleBetween < PI - obstructionViewportAngle * .5f;
//            float angle = angleBetween + (obstructedFromTheRight ? -HALF_PI : HALF_PI);
            float adjustment = obstructedFromTheRight?HALF_PI:-HALF_PI;
            me.acc.add(new PVector(me.spd.x, me.spd.y).rotate(adjustment).normalize().mult(obstructionMag));

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

    private void displayDebugDirection(Boid boid, float dir) {
        if (!debug) {
            return;
        }
        pushMatrix();
        translate(boid.pos.x, boid.pos.y);
        rotate(dir);
        line(0, 0, 50 * boid.spd.mag(), 0);
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

    float lazyDistanceEval(Boid a, Boid b, float d) {
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

    boolean isBoidInRectangle(Boid a, Boid b, float range) {
        return isPointInRect(a.pos.x, a.pos.y,
                b.pos.x - range,
                b.pos.y - range,
                range * 2,
                range * 2);
    }

    private boolean isBehindPlayer(Boid who) {
        return abs(normalizedAngleToPlayerHeading(who)) < HALF_PI;
    }


    public float normalizedAngleToPlayerHeading(Boid who) {
        float angleToPlayer = angleToPlayer(who);
        float normalizedAngleToPlayer = normalizeAngle(angleToPlayer, PI);
        float normalizedPlayerHeading = normalizeAngle(player.spd.heading(), PI);
        return normalizeAngle(normalizedAngleToPlayer - normalizedPlayerHeading, 0);
    }

    public float angleToPlayer(Boid who) {
        return angleToB(who, player);
    }

    private float angleToB(Boid a, Boid b) {
        return angleToB(a.pos, b.pos);
    }

    private float angleToB(PVector a, PVector b) {
        return atan2(b.y - a.y, b.x - a.x);
    }

    public float normalizeAngle(float a, float center) {
        return a - TWO_PI * floor((a + PI - center) / TWO_PI);
    }

    float angleDifference(float angleStart, float angleTarget) {
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
        public float beakLength = 2 + abs(randomGaussian());
        public float bodyLength = 10 + abs(randomGaussian());
        public float bodyWidth = 3 + abs(randomGaussian());
        public float tailSize = 2 + abs(randomGaussian());
        public float wingWidth = 10 + abs(randomGaussian());
        public float wingHeight = 2 + abs(randomGaussian());
        PVector pos;
        PVector spd = new PVector();
        PVector acc = new PVector();
        boolean dead = false;
        boolean isPlayer = false;

        Boid() {
        }

        Boid(PVector spawnPos) {
            pos = spawnPos;
        }
    }


}
