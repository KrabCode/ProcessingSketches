import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class FlowField extends KrabApplet {
    ArrayList<Particle> particles = new ArrayList<Particle>();
    ArrayList<Particle> particlesToRemove = new ArrayList<Particle>();
    private PGraphics pg;

    public static void main(String[] args) {
        FlowField.main("FlowField");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.smooth(16);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        frameRecordingDuration *= 4;
        pg.blendMode(ADD);
    }

    public void draw() {
        pg.beginDraw();
        alphaFade(pg);
        updateParticles();
        pg.endDraw();
        image(pg, 0, 0);
        vignettePass(pg);
        rec(pg);
        gui();
    }

    private void updateParticles() {
        if (button("reset particles")) {
            pg.background(0);
            particles.clear();
        }
        int particleCount = sliderInt("count", 10000);
        while (particleCount < particles.size()) {
            particles.remove(0);
        }
        while (particleCount > particles.size()) {
            particles.add(new Particle());
        }
        for (Particle p : particles) {
            p.update();
        }
        particles.removeAll(particlesToRemove);
        particlesToRemove.clear();
    }

    class Particle {
        PVector pos, spd;
        boolean add = random(1) > .5f;
        int frameBorn = frameCount;
        float hueOffset = random(1), satOffset = random(1);
        float removeBuffer = width / 2f;

        Particle() {
            if (toggle("gaussian spawn", true)) {
                pos = getGaussianCenterPos();
            } else {
                pos = getPosOutsideScreenButInsideBounds();
            }
            spd = new PVector();
        }

        private PVector getGaussianCenterPos() {
            float spread = slider("spread");
            PVector translate = sliderXYZ("gauss translate");
            return new PVector(width * .5f + translate.x + randomGaussian() * spread,
                    height * .5f + translate.y + randomGaussian() * spread);
        }

        private PVector getPosOutsideScreenButInsideBounds() {
            PVector pos = new PVector(random(-removeBuffer, width + removeBuffer * 2),
                    random(-removeBuffer, height + removeBuffer * 2));
            while (isPointInRect(pos.x, pos.y, 0, 0, width, height)) {
                pos = new PVector(random(-removeBuffer, width + removeBuffer * 2),
                        random(-removeBuffer, height + removeBuffer * 2));
            }
            return pos;
        }

        void update() {
            float toCenterAngle = atan2(height * .5f - pos.y, width * .5f - pos.x);
            float sideways = toCenterAngle + HALF_PI;
            float yNorm = clampNorm(pos.y,0,height);
            float noiseAngle = HALF_PI+(1.f-yNorm) * slider("noise angles") *
                    noise(pos.x * slider("x freq"),pos.y * slider("y freq"));
            PVector acc = PVector.fromAngle(noiseAngle).mult(slider("noise acc"));
            acc.add(PVector.fromAngle(toCenterAngle).mult(slider("to center acc")));
            acc.add(PVector.fromAngle(sideways).mult(slider("sideways acc")));
            spd.add(acc);
            spd.mult(slider("drag", .95f));
            pos.add(spd);
            HSBA stroke = picker("stroke");
            float fadeInFrames = slider("fade in frames", 10);
            float fadeIn = easeNorm(frameCount, frameBorn, frameBorn + fadeInFrames, slider("fade in ease"));
            pg.stroke(hueModulo(stroke.hue() + hueOffset * slider("hue offset")),
                    stroke.sat() + satOffset * slider("sat offset"), stroke.br(),
                    stroke.alpha() * fadeIn);
            pg.strokeWeight(slider("weight", 2));
            pg.point(pos.x, pos.y);
            boolean isOutsideBounds = !isPointInRect(pos.x, pos.y, -removeBuffer, -removeBuffer,
                    width + removeBuffer * 2,height + removeBuffer * 2);
            boolean isInCenter = isPointInRect(pos.x, pos.y, width / 2f - 1, height / 2f - 1, 2, 2);
            if (isOutsideBounds || isInCenter) {
                particlesToRemove.add(this);
            }
        }
    }
}
