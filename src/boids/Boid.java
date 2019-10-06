package boids;

import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

@SuppressWarnings("AccessStaticViaInstance")
public class Boid {
    public PVector pos = new PVector();
    public PVector spd = new PVector();
    public PVector acc = new PVector();
    public PVector lastSpd = new PVector();
    public boolean dead = false;
    public boolean isPlayer = false;
    public float distanceToClosestBoid, alignMagNow, alignMagNorm, centerMagNow, centerMagNorm, avoidMagNow, avoidMagNorm, obstacleMagNow, obstacleMagNorm, avgTurningRate, avgTurningRateNorm;
    private Boids p;
    private PGraphics pg;
    private ArrayList<Float> lastFewHeadingChanges = new ArrayList<Float>();
    private float flapStart, bodyLength, bodyWidth, wingWidth, wingHeight, tailRadius, tailAngle;
    private int featherInvulnerabilityDuration = 120;
    private int lastFeatherCloud = -featherInvulnerabilityDuration;
    private float flapTime;
    private float glideTime;

    Boid(Boids p, PGraphics pg) {
        this.pg = pg;
        this.p = p;
        setup();
    }

    Boid(Boids p, PGraphics pg, PVector spawnPos) {
        this.pg = pg;
        this.pos = spawnPos;
        this.p = p;
        setup();
    }

    void setup() {
        flapStart = p.random(1);
        bodyLength = 10 + p.randomGaussian();
        bodyWidth = bodyLength * .15f;
        wingWidth = bodyLength * 2.5f;
        wingHeight = bodyLength * .35f;
        tailRadius = bodyLength * .5f;
        tailAngle = .8f + p.randomGaussian() * .2f;
    }

    void update() {
        alignMagNorm = p.norm(alignMagNow, 0, p.alignMag);
        avoidMagNorm = p.norm(avoidMagNow, 0, .2f * p.avoidMag);
        centerMagNorm = p.norm(centerMagNow, 0, p.centerMag);
        obstacleMagNorm = p.norm(obstacleMagNow, 0, p.obstacleMag);
        avgTurningRateNorm = p.constrain(p.norm(avgTurningRate, 0.0f, .02f), 0, 1);
        flapTime += p.radians(avgTurningRateNorm * 10);
        glideTime += p.constrain(p.radians(avgTurningRateNorm * 10), 0, 1);

        if (avoidMagNorm > 1.0f && p.frameCount > lastFeatherCloud + featherInvulnerabilityDuration) {
            p.particles.addAll(p.featherParticleCloud(pos));
            lastFeatherCloud = p.frameCount;
        }

        if (avoidMagNorm > 3.0f) {
            p.kill(this);
        }

        spd.add(acc);
        float angularDifferenceFromLastSpd = p.angleDifference(lastSpd.heading(), spd.heading());
        float maximumAllowedAngularDifference = p.radians(p.maxRot);
        if (p.abs(angularDifferenceFromLastSpd) > maximumAllowedAngularDifference) {
            spd.rotate(-angularDifferenceFromLastSpd);
            if (angularDifferenceFromLastSpd > 0) {
                spd.rotate(maximumAllowedAngularDifference);
            } else {
                spd.rotate(-maximumAllowedAngularDifference);
            }
        }
        if (spd.mag() < p.minSpd) {
            if (spd.mag() == 0) {
                spd.add(PVector.random2D());
            }
            spd.setMag(p.minSpd);
        }
        spd.mult(p.drag);
        pos.add(spd);
        acc.mult(0);
        avgTurningRate = calculateAvgTurningRate();

        lastSpd.x = spd.x;
        lastSpd.y = spd.y;
    }

    private float calculateAvgTurningRate() {
        float majorHeadingChangeThreshold = p.radians(3);
        float change = p.abs(p.angleDifference(spd.heading(), lastSpd.heading()));
        if(change > majorHeadingChangeThreshold){
            lastFewHeadingChanges.clear();
        }
        lastFewHeadingChanges.add(change);
        int headingChangeMemory = 60;
        if (lastFewHeadingChanges.size() > headingChangeMemory) {
            lastFewHeadingChanges.remove(0);
        }
        float sum = 0;
        for (Float f : lastFewHeadingChanges) {
            sum += f;
        }
        int count = lastFewHeadingChanges.size();
        return p.max(change / count, sum / count);
    }

    void display(float t) {

        pg.pushMatrix();
        pg.translate(pos.x, pos.y);
        pg.scale(p.scl);
        pg.rotate(spd.heading());
        if (p.debug) {
            pg.fill(255 - 255 * p.max(alignMagNorm, obstacleMagNorm),
                    255 - 255 * p.max(avoidMagNorm, obstacleMagNorm),
                    255 - 255 * p.max(avoidMagNorm, alignMagNorm));
        } else {
            pg.fill(255);
        }
        pg.noStroke();
        pg.ellipseMode(p.CENTER);
        body(bodyLength, bodyWidth);

        pg.translate(-bodyLength * .05f, 0);
        tail(tailRadius, tailAngle);

        wing(wingWidth, wingHeight, t, false);
        wing(wingWidth, wingHeight, t, true);

        pg.popMatrix();
    }

    private void body(float bodyLength, float bodyWidth) {
        pg.ellipse(0, 0, bodyLength, bodyWidth);
    }

    private void tail(float tailRadius, float tailAngle) {
        int res = 7;
        pg.beginShape(p.TRIANGLE_STRIP);
        for (int i = 0; i < res; i++) {
            float inorm = p.norm(i, 0, res - 1);
            float angle = p.map(inorm, 0, 1, p.PI - tailAngle * .5f, p.PI + tailAngle * .5f);
            float x = tailRadius * p.cos(angle);
            float y = tailRadius * p.sin(angle);
            pg.vertex(0, 0);
            pg.pushStyle();
            pg.fill(0);
            pg.vertex(x, y);
            pg.popStyle();
        }
        pg.endShape();
    }

    private void wing(float wingSpan, float wingHeight, float t, boolean right) {
        float wingSign = right ? -1 : 1;
        pg.beginShape(p.TRIANGLE_STRIP);
        int res = 7;
        for (int i = 0; i < res; i++) {
            float inorm = p.norm(i, 0, res - 1);
            float oddSign = i % 2 == 0 ? -1 : 1;
            float taper = p.constrain(inorm > .5f ? 2 - inorm * 1.8f : 1, 0, 1);
            float flap = inorm * p.sin(inorm * 5 - flapTime + flapStart * 10);
            float glide = inorm * p.sin(inorm * 5 - glideTime + flapStart * 10);
            float flapGlide = p.lerp(glide, flap, avgTurningRateNorm);
            float y = wingSign * wingSpan * .5f * inorm;
            float x = wingHeight * .5f * taper;
            pg.noStroke();
            if (i == 0) {
                pg.vertex(x, y);
            }
            if (oddSign < 0) {
                pg.pushStyle();
                pg.fill(0);
            }
            pg.vertex(oddSign * x + flapGlide * wingHeight * .5f, y);
            if (oddSign < 0) {
                pg.popStyle();
            }
        }
        pg.endShape();
    }

}
