import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class MirroredParticles extends KrabApplet {
    private PVector center;
    private PGraphics pg;
    private ArrayList<Particle> particles = new ArrayList<Particle>();

    public static void main(String[] args) {
        MirroredParticles.main("MirroredParticles");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.background(0);
        pg.endDraw();
        center = new PVector(width, height).mult(.5f);
        recordingFrames *= 2;
    }

    public void draw() {
        pg.beginDraw();
        alphaFade(pg);
        radialBlurPass(pg);
        pg.translate(center.x, center.y);
        pg.strokeWeight(slider("weight", 1));
        int mirrors = sliderInt("mirrors", 0, 100, 1);
        group("particle");
        HSBA hsba = picker("stroke");
        hsba.addHue(radians(slider("add hue")) / TWO_PI);
        updateParticles();
        for (int i = 0; i <= mirrors; i++) {
            float iNorm = norm(i, 0, mirrors);
            pg.pushMatrix();
            pg.rotate(iNorm * TWO_PI);
            for (Particle p : particles) {
                pg.stroke((hsba.hue() + p.hueOffset * slider("hue offset")) % 1,
                        constrain(hsba.sat() + p.satOffset * slider("sat offset"), 0, 1),
                        hsba.br());
                pg.stroke(hsba.clr());
                pg.line(p.pos.x - center.x, p.pos.y - center.y, p.prev.x - center.x, p.prev.y - center.y);
            }
            pg.popMatrix();
        }
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }


    private void updateParticles() {
        int count = sliderInt("count", 20);
        while (particles.size() < count) {
            particles.add(new Particle());
        }
        while (particles.size() > count) {
            particles.remove(0);
        }
        for (Particle p : particles) {
            p.update();
        }
    }

    class Particle {
        PVector pos = new PVector(random(width), random(height)), spd = new PVector();
        PVector prev = pos.copy();
        float hueOffset = randomGaussian();
        float dragOffset = random(1);
        float satOffset = random(1);

        void update() {
            PVector toCenter = PVector.sub(center, pos);
            spd.add(toCenter.copy().rotate(HALF_PI).normalize().mult(slider("orbit")));
            spd.add(toCenter.normalize().mult(slider("to center")));
            spd.add(new PVector(randomGaussian(), randomGaussian()).mult(slider("gauss")));
            spd.mult(slider("drag") * dragOffset);
            prev = pos.copy();
            pos.add(spd);
        }
    }
}