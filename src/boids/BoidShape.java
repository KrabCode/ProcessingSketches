package boids;

import applet.HotswapGuiSketch;
import processing.core.PVector;

public class BoidShape extends HotswapGuiSketch {
    public static void main(String[] args) {
        HotswapGuiSketch.main("boids.BoidShape");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    Boid boid;
    float t;

    public void setup() {
        boid = new Boid();
        boid.pos.x = width * .5f;
        boid.pos.y = height * .5f;
    }

    public void draw() {
        if (button("regen")) {
            boid = new Boid();
            boid.pos.x = width * .5f;
            boid.pos.y = height * .5f;
        }
        t += radians(slider("t", 0, 1, 1));
        background(0);
        pushMatrix();
        translate(boid.pos.x, boid.pos.y);
        scale(slider("scl", 30));
        rotate(boid.spd.heading());
        fill(255);
        noStroke();
        strokeWeight(1);
        ellipseMode(CENTER);

        float bodyLength = slider("body length", 20);
        float bodyWidth = slider("body width", 3);
        float wingWidth = slider("wing width", 50);
        float wingHeight = slider("wing height", 7);
        float tailRadius = bodyLength*.5f;
        float tailAngle = slider("tail angle", 0, PI, .8f);
        fill(255);
        body(bodyLength, bodyWidth);
        translate(-bodyLength * slider("wing offset", 0.1f), 0);
        tail(tailRadius, tailAngle);
        wing(wingWidth, wingHeight, false);
        wing(wingWidth, wingHeight, true);
        popMatrix();
        rec();
        gui();
    }

    private void body(float bodyLength, float bodyWidth) {
        ellipse(0, 0, bodyLength, bodyWidth);
    }

    void tail(float tailRadius, float tailAngle) {
        int res = floor(slider("tailRes", 14));
        beginShape(TRIANGLE_STRIP);
        for(int i = 0; i < res; i++){
            float inorm = norm(i, 0, res-1);
            float angle = map(inorm, 0, 1, PI - tailAngle*.5f, PI+tailAngle*.5f);
            float x = tailRadius*cos(angle);
            float y = tailRadius*sin(angle);
            fill(255);
            vertex(0,0);
            fill(0);
            vertex(x,y);
        }
        endShape();
    }

    void wing(float wingSpan, float wingHeight, boolean right) {
        float wingSign = right ? -1 : 1;
        beginShape(TRIANGLE_STRIP);
        int res = floor(slider("wing res", 14));
        for (int i = 0; i < res; i++) {
            float inorm = norm(i, 0, res - 1);
            float oddSign = i % 2 == 0 ? -1 : 1;
            float taper = constrain( inorm > .5f ? 2 - inorm * slider("taper", 3.6f) : 1, 0, 1);
            float flap = inorm * sin(inorm * slider("flap freq", 10) - t * slider("flap spd", 20));
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
            vertex(oddSign * wingHeight * .5f * taper + flap * wingHeight * slider("flap mag"),
                    wingSign * wingSpan * .5f * inorm);
        }
        endShape();
    }

    private class Boid {
        PVector pos = new PVector();
        PVector spd = new PVector();
        PVector acc = new PVector();

        Boid() {
        }

        Boid(PVector spawnPos) {
            pos = spawnPos;
        }
    }
}
