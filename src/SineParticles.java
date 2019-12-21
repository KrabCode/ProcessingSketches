import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class SineParticles extends KrabApplet {
    ArrayList<Particle> particles = new ArrayList<Particle>();
    ArrayList<Particle> particlesToRemove = new ArrayList<Particle>();
    PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("MultitorusParticles");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        surface.setLocation(1920 - 820, 20);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        group("shaders");
        alphaFade(pg);
        splitPass(pg);
        pg.translate(pg.width * .5f, pg.height * .5f);
        updateParticles();
        pg.endDraw();
        image(pg, 0, 0, width, height);
        rec(pg);
        gui();
    }

    private void updateParticles() {
        group("particles");
        int intendedParticleCount = sliderInt("particle count");
        while (particles.size() < intendedParticleCount) {
            particles.add(new Particle());
        }
        while (particles.size() > intendedParticleCount) {
            particles.remove(particles.size() - 1);
        }
        for (Particle p : particles) {
            p.update();
        }
        particles.removeAll(particlesToRemove);
        particlesToRemove.clear();
    }

    class Particle {
        PVector pos, spd;
        float fadeInDuration;
        float frameStarted = frameCount;
        float hueOffset = random(-1, 1);
        float satOffset = random(-1, 1);

        public Particle() {
            this.pos = PVector.fromAngle(random(TAU*2)).setMag(width+random(width));
            this.spd = new PVector();
            fadeInDuration = slider("fade in duration");
        }

        PVector getForce() {
            // no z
            float freq = slider("freq");
            return new PVector(sin(pos.x * freq), sin(pos.y * freq));
        }

        void update() {
            PVector acc = getForce();
            acc.limit(slider("acc limit"));
            PVector toCenter = PVector.sub(new PVector(), pos);
            toCenter.mult(slider("to center"));
            acc.mult(slider("acc"));
            acc.add(toCenter);
            PVector random = new PVector(randomGaussian(), randomGaussian(), randomGaussian()).mult(slider("random"));
            acc.add(random);
            spd.add(acc);
            if(spd.mag() < slider("spd garbage threshold")){
                particlesToRemove.add(this);
            }
            spd.mult(slider("drag"));
            pos.add(spd);
            pg.colorMode(HSB, 1, 1, 1, 1);
            HSBA hsba = picker("stroke");
            float fadeIn = constrain(norm(frameCount, frameStarted, frameStarted + fadeInDuration), 0, 1);
            pg.stroke(hueModulo(hsba.hue() + hueOffset * slider("hue offset")),
                    hsba.sat() + satOffset * slider("sat offset"),
                    hsba.br(),
                    min(hsba.alpha(), fadeIn));
            pg.strokeWeight(slider("weight"));
            pg.point(pos.x, pos.y, pos.z);
        }
    }
}
