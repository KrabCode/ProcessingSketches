import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class Clouds extends HotswapGuiSketch {
    public static void main(String[] args) {
        HotswapGuiSketch.main("Clouds");
    }

    private float t;
    private float particleLifespan, emitterGaussian, intendedEmitterCount, sizeLow, sizeHigh, arcLow, arcHigh, particleMaxY;
    private PGraphics pg;
    private ArrayList<Particle> particles = new ArrayList<Particle>();
    private ArrayList<Particle> particlesToRemove = new ArrayList<>();
    private ArrayList<Emitter> emitters = new ArrayList<Emitter>();

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        sizeLow = slider("size low", 20);
        sizeHigh = slider("size high", 140);
        arcLow = slider("arc low", PI, false);
        arcHigh = slider("arc high", PI, true);
        intendedEmitterCount = slider("emitter count", 40);
        emitterGaussian = slider("emitter gauss", 300);
        particleLifespan = slider("lifespan", 280 * 2);
        particleMaxY = slider("max y", 0,height, height*.8f);
        pg.beginDraw();
        alphaFade(pg);
        pg.strokeWeight(slider("w", 4));
        pg.noFill();
        updateParticleSystem();
        rgbSplitUniformPass(pg);
        noisePass(t, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void updateParticleSystem() {
        while (emitters.size() < intendedEmitterCount) {
            emitters.add(new Emitter());
        }
        while (emitters.size() > intendedEmitterCount) {
            emitters.remove(emitters.size() - 1);
        }
        for (Emitter e : emitters) {
            e.update();
        }
        for (Particle p : particles) {
            p.update();
        }
        particles.removeAll(particlesToRemove);
        particlesToRemove.clear();
    }

    class Particle {
        PVector pos, spd = new PVector();
        Emitter emitter;
        int frameCreated;
        float size = random(sizeLow, sizeHigh);
        float arc = random(arcLow, arcHigh);

        Particle(Emitter emitter) {
            this.emitter = emitter;
            pos = PVector.add(emitter.pos, PVector.random2D());
            frameCreated = frameCount;
        }

        public void update() {
            PVector fromEmitter = PVector.sub(pos, emitter.pos);
            float normalizedDistance = norm(fromEmitter.mag(), 0, slider("distance ceiling"));
            PVector acc = fromEmitter.normalize().setMag(1 / normalizedDistance);
            spd.add(acc);
            spd.mult(slider("drag", .8f, .99f));
            pos.add(spd);
            if(pos.y > particleMaxY){
                pos.y = particleMaxY;
            }
            float lifeNorm = constrain(norm(frameCount, frameCreated, frameCreated + particleLifespan), 0, 1);
            pg.noStroke();
            if (lifeNorm == 1) {
                particlesToRemove.add(this);
            }
            float alpha = 255 - 255 * 2 * abs(.5f - lifeNorm);
            pg.push();
            pg.translate(pos.x, pos.y);
            pg.rotate(fromEmitter.heading());
            if (toggle("fill", true)) {
                pg.fill(0, constrain(alpha*2, 0, 255));
                pg.noStroke();
                pg.ellipse(0,0,size,size);
                pg.noFill();
                pg.stroke(255, alpha);
                pg.arc(0, 0, size, size, -arc * .5f, arc * .5f);
            } else {
                pg.noFill();
                pg.stroke(255, alpha);
                pg.arc(0, 0, size, size, -arc * .5f, arc * .5f);
            }
            pg.pop();
        }
    }

    class Emitter {
        PVector pos = new PVector(width * .5f, height * .5f).add(randomGaussian() * emitterGaussian, randomGaussian() * emitterGaussian);
        int emitterFreqOffset = floor(random(10));

        public void update() {
            if ((frameCount + emitterFreqOffset) % floor(slider("emit freq", 1, 20)) == 0) {
                particles.add(new Particle(this));
            }
        }
    }
}
