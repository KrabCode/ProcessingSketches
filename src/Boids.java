import applet.HotswapGuiSketch;
import processing.core.PVector;

import java.util.ArrayList;

public class Boids extends HotswapGuiSketch {

    //TODO
    // - pretty birds
    // - pretty fish
    // - angle of sight for behaviors with nice debug arcs
    // - efficient division, compute shaders?
    // - camera moving with player
    // - food chain
    // - player takes control of eater when eaten
    // - player gets bigger as he eats

    private static final float INITIAL_DISTANCE_VALUE = -1;

    ArrayList<Boid> boids = new ArrayList<Boid>();
    ArrayList<Boid> toRemove = new ArrayList<Boid>();
    boolean targetActive;
    float avoidRange, avoidMag, centerRange, centerMag, alignRange, alignMag, perpendicularAvoidRange, perpendicularAvoidMag;
    float farAway, centerToCornerDistance;
    private PVector cameraOffset, playerTarget;

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
        Boid player = new Boid();
        player.pos = new PVector();
        playerTarget = randomPositionOffscreenInFrontOfPlayer(player);
        player.isPlayer = true;
        boids.add(player);
    }

    public void draw() {
        bg();
        updateCamera();
        updateBoids();
        rec();
        gui();
    }

    private void bg() {
        background(0);
    }

    private void updateCamera() {
        Boid player = getPlayer();
        float cameraTightness = .3f;
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
        removeDeadBoids();
        Boid player = getPlayer();
        updateBoidCount(player);
        avoidRange = slider("avoid r", 40);
        avoidMag = slider("avoid mag", 1);
        centerRange = slider("center r", 200);
        centerMag = slider("center mag", .5f);
        alignRange = slider("align r", 100);
        alignMag = slider("align mag", .25f);
        perpendicularAvoidRange = slider("perp avoid r", 200);
        perpendicularAvoidMag = slider("perp avoid mag", 1);
        for (Boid boid : boids) {
            reactToOtherBoids(boid);
            if (boid.isPlayer) {
                stroke(255);
                updatePlayerTarget(player);
                updatePlayer(boid);
            } else {
                if (isBehindPlayer(boid, player) && dist(player.pos.x, player.pos.y, boid.pos.x, boid.pos.y) > farAway) {
                    boid.dead = true;
                }
            }
            noFill();
            strokeWeight(1);
            ellipse(boid.pos.x, boid.pos.y, 10, 10);

            boid.spd.add(boid.acc);
            float minMag = slider("minMag");
            if (boid.spd.mag() < minMag) {
                boid.spd.setMag(minMag);
            }
            boid.acc.mult(0);
            boid.spd.mult(slider("drag", .5f,1));
            boid.pos.add(boid.spd);
        }
    }

    private void reactToOtherBoids(Boid me) {
        PVector towardsCenterSum = new PVector();
        int towardsCenterCount = 0;
        PVector alignAvgSpd = new PVector();
        int alignSpdCount = 0;

        for (Boid other : boids) {
            if (me.equals(other)) {
                continue;
            }
            //avoid boids that are too close
            float d = INITIAL_DISTANCE_VALUE;
            PVector avoid = new PVector();
            if (isBoidInRectangle(me, other, avoidRange)) {
                d = dist(me.pos.x, me.pos.y, other.pos.x, other.pos.y);
                if (d < avoidRange) {
                    PVector thisAvoidance = PVector.sub(me.pos, other.pos);
                    avoid.add(thisAvoidance.normalize().mult(1 / thisAvoidance.mag()).mult(avoidMag));
                    stroke(255, 100, 100);
                    ellipse(me.pos.x, me.pos.y, 10, 10);
                }
            }
            me.acc.add(avoid);

            // the a boid is not avoiding so it can do something else
            if (avoid.mag() < .01f) {
                stroke(255);
                // boid a moves towards others inside center range
                if (isBoidInRectangle(me, other, centerRange)) {
                    d = lazyDistanceCalc(me, other, d);
                    if (d < centerRange) {
                        towardsCenterSum.add(PVector.sub(other.pos, me.pos));
                        towardsCenterCount++;
                        stroke(100, 255, 100);
                        ellipse(me.pos.x, me.pos.y, centerRange * 2, centerRange * 2);
                    }
                }
                // align
                if (isBoidInRectangle(me, other, alignRange)) {
                    d = lazyDistanceCalc(me, other, d);
                    if (d < alignRange) {
                        alignAvgSpd.add(other.spd);
                        alignSpdCount++;
                        stroke(100, 100, 255);
                        ellipse(me.pos.x, me.pos.y, alignRange * 2, alignRange * 2);
                    }
                }

                if (isBoidInRectangle(me, other, perpendicularAvoidRange)) {
                    //TODO perpendicular avoidance
                }
            }
        }

        if (towardsCenterCount > 0) {
            me.acc.add(towardsCenterSum.div(towardsCenterCount).normalize().mult(centerMag));
        }
        if (alignSpdCount > 0) {
            PVector avgSpd = alignAvgSpd.div(alignSpdCount);
            me.acc.add(avgSpd.normalize().mult(alignMag));
        }
    }

    float lazyDistanceCalc(Boid a, Boid b, float d){
        if (d == INITIAL_DISTANCE_VALUE) {
            d = dist(a.pos.x, a.pos.y, b.pos.x, b.pos.y);
        }
        return d;
    }

    private boolean isBehindPlayer(Boid who, Boid player) {
        float angleToPlayer = atan2(player.pos.y - who.pos.y, player.pos.x - who.pos.x);
        float normalizedAngleToPlayer = normalizeAngle(angleToPlayer, PI);
        float normalizedPlayerHeading = normalizeAngle(player.spd.heading(), PI);
        float angleToPlayerVsHeading = normalizeAngle(normalizedAngleToPlayer - normalizedPlayerHeading, 0);
        return abs(angleToPlayerVsHeading) < HALF_PI;
    }

    private void updatePlayer(Boid player) {
        stroke(255);
        strokeWeight(1);
        ellipse(player.pos.x, player.pos.y, avoidRange * 2, avoidRange * 2);

        pushMatrix();
        translate(player.pos.x, player.pos.y);
        rotate(player.spd.heading());
        float r = norm(player.spd.heading(), -PI, PI);
        float b = 1-r;
        stroke(255*r, 0, 255*b);
        line(0, 0, 50*player.spd.mag(), 0);
        popMatrix();

        float mouseTightness = slider("mouse tight", 6);
        if (targetActive) {
            PVector towardsTarget = PVector.sub(playerTarget, player.pos);
            player.acc.add(towardsTarget.normalize().mult(mouseTightness));
        }
    }

    private void updatePlayerTarget(Boid player) {
        if (mousePressedOutsideGui) {
            playerTarget.x = mouseX - cameraOffset.x;
            playerTarget.y = mouseY - cameraOffset.y;
            targetActive = true;
        }
        if (!mousePressedOutsideGui || dist(player.pos.x, player.pos.y, playerTarget.x, playerTarget.y) < 5) {
            targetActive = false;
        }
    }

    private void updateBoidCount(Boid player) {
        int intendedBoidCount = floor(slider("count", 1, 60));
        while (boids.size() < intendedBoidCount) {
            boids.add(new Boid(randomPositionOffscreenInFrontOfPlayer(player)));
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

    public float normalizeAngle(float a, float center) {
        return a - TWO_PI * floor((a + PI - center) / TWO_PI);
    }

    private PVector randomPositionOffscreenInFrontOfPlayer(Boid player) {
        float angle = random(player.spd.heading() - HALF_PI, player.spd.heading() + HALF_PI);
        float distance = random(centerToCornerDistance, farAway);
        return new PVector(player.pos.x + distance * cos(angle), player.pos.y + distance * sin(angle));
    }

    private class Boid {
        PVector pos;
        PVector spd = new PVector();
        PVector acc = new PVector();
        float dir = random(TWO_PI);
        boolean dead = false;
        boolean isPlayer = false;

        Boid() {
        }

        Boid(PVector spawnPos) {
            pos = spawnPos;
        }
    }


}
