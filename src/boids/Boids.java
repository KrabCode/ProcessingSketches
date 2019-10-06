package boids;

import applet.HotswapGuiSketch;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class Boids extends HotswapGuiSketch {

    //TODO
    // - efficient division, compute shaders?
    // - food chain
    // - player takes control of eater when eaten
    // - player gets bigger as he eats

    private static final float INITIAL_DISTANCE_VALUE = -1;
    static Boid player;
    public float drag, scl, minSpd, maxRot;
    public ArrayList<Particle> particles = new ArrayList<Particle>();
    protected float farAway, centerToCornerDistance;
    ControllerManager controllers;
    PGraphics main, bg, fg;
    float alignMag, obstacleMag, centerMag, avoidMag;
    float avoidRadius;
    boolean debug;
    private int intendedBoidCount;
    private ArrayList<Boid> boids = new ArrayList<Boid>();
    private ArrayList<Boid> toRemove = new ArrayList<Boid>();
    private boolean targetActive;
    private float centerRadius;
    private float alignRadius;
    private float obstacleRadius;
    private float obstacleAngle;
    private PVector cameraOffset, playerTarget;
    private float t;
    private ArrayList<Particle> particlesToRemove = new ArrayList<Particle>();
    private int particleBurstCount;
    private int margin = 100;

    public static void main(String[] args) {
        HotswapGuiSketch.main("boids.Boids");
    }

    public void settings() {
        size(800, 800, P2D);
//        fullScreen(P2D, 2);
        recordingFrames *= 3;
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        main = createGraphics(width + margin, height + margin, P2D);
        bg = createGraphics(width + margin, height + margin, P2D);
        fg = createGraphics(width + margin, height + margin, P2D);
        bg.beginDraw();
        bg.background(0);
        bg.endDraw();

        controllers = new ControllerManager();
        controllers.initSDLGamepad();
        cameraOffset = new PVector(width * .5f, height * .5f);
        centerToCornerDistance = dist(0, 0, width * .5f, height * .5f);
        farAway = centerToCornerDistance * 2;
        player = new Boid(this, fg);
        player.pos = new PVector();
        playerTarget = randomPositionOffscreenInFrontOfPlayer();
        player.isPlayer = true;
        boids.add(player);
    }

    public void draw() {
        t += radians(slider("t", 0, 1, 1));
        sliders();
        bg.beginDraw();
        fg.beginDraw();
        updateCamera();
        fg.clear();
        updateBoids();
        updateParticles();
        fg.endDraw();
        seaPass(bg);
        bg.endDraw();

        main.beginDraw();
        main.image(bg, 0,-margin);
        main.blendMode(SUBTRACT);
        main.image(fg,
                slider("shadow x", -100, 100, -10),
                slider("shadow y", -100, 100, -21));
        main.blendMode(BLEND);
        main.image(fg, 0, 0);
        cloudPass(main);
        main.endDraw();

        image(main, 0, 0);
        rec(main);
        gui(false);
    }

    private void seaPass(PGraphics pg) {
        String sea = "boids\\sea.glsl";
        uniform(sea).set("time", t);
        uniform(sea).set("camera", cameraOffset.x, cameraOffset.y);
        hotFilter(sea, pg);
    }

    private void cloudPass(PGraphics pg) {
        String clouds = "boids\\clouds.glsl";
        uniform(clouds).set("time", t);
        uniform(clouds).set("camera", cameraOffset.x, cameraOffset.y);
        hotFilter(clouds, pg);
    }

    private void sliders() {
        debug = toggle("debug", false);
        drag = slider("drag", .5f, 1, .85f);
        maxRot = slider("max rotation", 6);
        scl = slider("scl", 3.2f);
        minSpd = slider("minimum speed", 5);
        intendedBoidCount = floor(slider("count", 2, 501, 60));
        particleBurstCount = floor(slider("blood res", 50));
        if (frameCount == 1 || toggle("rules")) {
            avoidRadius = slider("avoid r", 40);
            avoidMag = slider("avoid mag", 2.f);
            obstacleRadius = slider("obst r", 200);
            obstacleMag = slider("obst mag", 0, .2f);
            obstacleAngle = slider("obst angle", 0, PI, 0.6f);
            centerRadius = slider("center r", 300);
            centerMag = slider("center mag", .02f);
            alignRadius = slider("align r", 200);
            alignMag = slider("align mag", .24f);
        }
    }

    private void updateCamera() {
        Boid player = getPlayer();
        float cameraTightness = .05f; // slider("cam tight", .3f);
        cameraOffset.x = lerp(cameraOffset.x, width * .5f - player.pos.x + margin * .5f, cameraTightness);
        cameraOffset.y = lerp(cameraOffset.y, height * .5f - player.pos.y + margin * .5f, cameraTightness);
        bg.translate(cameraOffset.x, cameraOffset.y);
        fg.translate(cameraOffset.x, cameraOffset.y);
    }

    private void updateParticles() {
        for (Particle p : particles) {
            p.update();
            p.display();
            if (p.isDead()) {
                particlesToRemove.add(p);
            }
        }
        particles.removeAll(particlesToRemove);
        particlesToRemove.clear();
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
            boid.display(t);
            boid.update();
        }
    }

    void kill(Boid boid) {
        boid.dead = true;
        particles.addAll(bloodParticleCloud(boid.pos));
        if (boid.isPlayer) {
            Boid randomBoid = randomBoid();
            while (randomBoid == player) {
                randomBoid = randomBoid();
            }
            randomBoid.isPlayer = true;
        }
    }

    public ArrayList<Particle> bloodParticleCloud(PVector pos) {
        int clr = color(150, 0, 0);
        return particleCloud(pos, 1, 7, clr);
    }

    public ArrayList<Particle> featherParticleCloud(PVector pos) {
        int clr = color(150);
        return particleCloud(pos, .6f, 3, clr);
    }

    private ArrayList<Particle> particleCloud(PVector pos, float scl, float acc, int clr) {
        ArrayList<Particle> cloud = new ArrayList<Particle>();
        int finalRes = floor(particleBurstCount + random(-3, 3));
        for (int i = 0; i < finalRes; i++) {
            float inorm = norm(i, 0, finalRes - 1);
            float angle = inorm * TWO_PI;
            Particle particle = new Particle(this, clr, scl, fg);
            particle.pos.x = pos.x;
            particle.pos.y = pos.y;
            particle.acc.add(PVector.fromAngle(angle).mult(acc * randomGaussian()));
            cloud.add(particle);
        }
        return cloud;
    }


    private Boid randomBoid() {
        return boids.get(floor(random(boids.size() - 1)));
    }

    private void reactToOtherBoids(Boid me) {
        me.distanceToClosestBoid = farAway;
        me.avoidMagNow = 0;
        me.obstacleMagNow = 0;
        me.centerMagNow = 0;
        me.alignMagNow = 0;
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
            if (isBoidInRectangle(me, other.pos, avoidRadius)) {
                d = dist(me.pos.x, me.pos.y, other.pos.x, other.pos.y);
                if (d < avoidRadius) {
                    PVector toMe = PVector.sub(me.pos, other.pos);
                    PVector avoidThis = toMe.normalize().div(d).mult(avoidMag);
                    displayDebugLine(other.pos, PVector.add(other.pos, toMe));
                    me.acc.add(avoidThis);
                    me.avoidMagNow += avoidThis.mag();

                    if (me.isPlayer) {
                        fg.stroke(255, 100, 100);
                        displayDebugCircle(me, avoidRadius);
                    }
                }
            }

            stroke(255);
            // the boid moves towards others inside center radius
            if (isBoidInRectangle(me, other.pos, centerRadius)) {
                d = lazyDistanceEval(me, other, d);
                if (d < centerRadius) {
                    towardsCenterSum.add(PVector.sub(other.pos, me.pos));
                    towardsCenterCount++;
                    if (me.isPlayer) {
                        fg.stroke(100, 255, 100);
                        displayDebugCircle(me, centerRadius);
                    }
                }
            }

            // the boid aligns its direction with others within align range
            if (isBoidInRectangle(me, other.pos, avoidRadius)) {
                d = lazyDistanceEval(me, other, d);
                if (d < alignRadius) {
                    alignHeadingSum.add(other.spd);
                    alignHeadingCount++;
                    if (me.isPlayer) {
                        fg.stroke(255, 100, 255);
                        displayDebugCircle(me, alignRadius);
                    }
                }
            }

            // find obstructing boids in a pie shaped perimeter in front of the bird
            if (isBoidInRectangle(me, other.pos, obstacleRadius)) {
                d = lazyDistanceEval(me, other, d);
                if (d < obstacleRadius) {
                    float angleDifference = angleDifferenceToMyHeading(me, other.pos);
                    if (abs(angleDifference) > PI - obstacleAngle * .5f) {
                        obstacleSum.add(other.pos);
                        obstacleCount++;

                        if (me.isPlayer) {
                            fg.stroke(40, 40, 200);
                            displayDebugArc(me, obstacleRadius, obstacleAngle);
                        }
                    }
                }

            }
        }

        //dividing by zero is very dangerous
        if (towardsCenterCount > 0) {
            PVector avg = towardsCenterSum.div(towardsCenterCount);
            PVector finalCorrection = avg.normalize().mult(centerMag);
            me.acc.add(finalCorrection);
            me.centerMagNow = finalCorrection.mag();
        }
        if (alignHeadingCount > 0) {
            PVector avg = alignHeadingSum.div(alignHeadingCount);
            float desiredAngle = avg.heading();
            float towardsDesiredAngle = angleDifference(me.spd.heading(), desiredAngle);
            float myRotation = lerp(0, towardsDesiredAngle, radians(alignMag));
            me.spd.rotate(myRotation);
            me.alignMagNow = abs(myRotation);
        }
        if (obstacleCount > 0) {
            PVector avg = obstacleSum.div(obstacleCount);
            float differenceToMyHeading = angleDifferenceToMyHeading(me, avg);
            float differenceToMyHeadingAbsNorm = norm(abs(differenceToMyHeading), 0, HALF_PI);
            float towardsDesiredAngle = differenceToMyHeading > 0 ? -QUARTER_PI : QUARTER_PI;
            float myRotation = lerp(0, towardsDesiredAngle, (1 / differenceToMyHeadingAbsNorm) * obstacleMag);
            me.spd.rotate(myRotation);
            me.obstacleMagNow = abs(myRotation);

            if (me.isPlayer) {
                fg.stroke(255, 0, 0);
                displayDebugDirection(me.pos, me.spd.heading() + towardsDesiredAngle);
            }
        }
    }

    private void updatePlayer() {
        if (targetActive) {
            PVector towardsTarget = PVector.sub(playerTarget, player.pos);
            player.acc.add(towardsTarget);
        }
    }

    Boid getPlayer() {
        for (Boid b : boids) {
            if (b.isPlayer) {
                return b;
            }
        }
        throw new IllegalStateException("No player found in boids list");
    }

    private void updatePlayerTarget() {
        ControllerState input = controllers.getState(0);
        if (input.rightStickMagnitude > .18) {
            targetActive = true;
            playerTarget.x = player.pos.x + input.rightStickX * abs(input.rightStickX) * .5f;
            playerTarget.y = player.pos.y - input.rightStickY * abs(input.rightStickY) * .5f;
        } else {
            targetActive = false;
        }
    }

    private void updateBoidCount() {
        while (boids.size() < intendedBoidCount) {
            boids.add(new Boid(this, fg, randomPositionOffscreenInFrontOfPlayer()));

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


    // ---------------------
    // MATH

    private float lazyDistanceEval(Boid me, Boid other, float d) {
        if (d == INITIAL_DISTANCE_VALUE) {
            d = dist(me.pos.x, me.pos.y, other.pos.x, other.pos.y);
        }
        if (d < me.distanceToClosestBoid) {
            me.distanceToClosestBoid = d;
        }
        return d;
    }

    private boolean isBoidInRectangle(Boid a, PVector center, float range) {
        return isPointInRect(a.pos.x,
                a.pos.y,
                center.x - range,
                center.y - range,
                range * 2,
                range * 2);
    }

    private boolean isBehindPlayer(Boid who) {
        return abs(normalizedAngleToPlayerHeading(who)) < HALF_PI;
    }

    private float normalizedAngleToPlayerHeading(Boid from) {
        float normalizedAngleToPlayer = normalizeAngle(angleToPlayer(from), PI);
        float normalizedPlayerHeading = normalizeAngle(player.spd.heading(), PI);
        return normalizeAngle(normalizedAngleToPlayer - normalizedPlayerHeading, 0);
    }

    private float angleToPlayer(Boid from) {
        return angleToB(from, player);
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

    public float angleDifference(float angleStart, float angleTarget) {
        float d = angleTarget - angleStart;
        while (d > PI) {
            d -= TWO_PI;
        }
        while (d < -PI) {
            d += TWO_PI;
        }
        return d;
    }

    private float angleDifferenceToMyHeading(Boid me, PVector other) {
        float heading = me.spd.heading();
        float angleBetween = angleToB(other, me.pos);
        return angleDifference(angleBetween, heading);
    }

    public PVector randomPositionOffscreenInFrontOfPlayer() {
        float angle = random(player.spd.heading() - HALF_PI, player.spd.heading() + HALF_PI);
        float distance = random(centerToCornerDistance, farAway);
        return new PVector(player.pos.x + distance * cos(angle), player.pos.y + distance * sin(angle));
    }

    // ---------------------
    // DEBUG

    @SuppressWarnings("unused")
    private void displayDebugLine(PVector a, PVector b) {
        if (!debug) {
            return;
        }
        fg.line(a.x, a.y, b.x, b.y);
    }

    private void displayDebugDirection(PVector origin, float dir) {
        if (!debug) {
            return;
        }
        fg.pushMatrix();
        fg.translate(origin.x, origin.y);
        fg.rotate(dir);
        fg.line(0, 0, 40, 0);
        fg.line(40, 0, 40 - 3, -3);
        fg.line(40, 0, 40 - 3, 3);
        fg.popMatrix();
    }

    private void displayDebugCircle(Boid boid, float radius) {
        if (!debug) {
            return;
        }
        fg.noFill();
        fg.ellipseMode(CENTER);
        fg.ellipse(boid.pos.x, boid.pos.y, radius * 2, radius * 2);
    }

    private void displayDebugArc(Boid boid, float radius, float angularSize) {
        if (!debug) {
            return;
        }
        fg.pushMatrix();
        fg.translate(boid.pos.x, boid.pos.y);
        fg.rotate(boid.spd.heading());
        fg.noFill();
        fg.ellipseMode(CENTER);
        fg.arc(0, 0, radius * 2, radius * 2, -angularSize * .5f, angularSize * .5f, PIE);
//      pg.line(0, 0, radius*cos(-angularSize*.5f), radius*sin(-angularSize*.5f));
//      pg.line(0, 0, radius*cos(angularSize*.5f), radius*sin(angularSize*.5f));
        fg.popMatrix();
    }

}
