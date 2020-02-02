import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-01
 */
public class RidgeParticles extends KrabApplet {

    ArrayList<Generator> generators = new ArrayList<>();
    ArrayList<Generator> generatorsToRemove = new ArrayList<>();
    ArrayList<Particle> particles = new ArrayList<>();
    ArrayList<Particle> particlesToRemove = new ArrayList<>();
    private PGraphics pg;
    private boolean shift = false;

    public void keyPressed() {
        super.keyPressed();
        if(keyCode == SHIFT){
            shift = !shift;
        }
    }

    public static void main(String[] args) {
        KrabApplet.main("RidgeParticles");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        group("general");
        pg.beginDraw();
        if (frameCount < 5) {
            pg.background(0);
        }
        if (button("clear")) {
            pg.background(0);
            particles.clear();
            generators.clear();
        }
        updateGenerators();
        updateParticles();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void updateGenerators() {
        group("generators");
        if (mouseJustPressedOutsideGui() && shift) {
            generators.add(new Generator(new PVector(mouseX, mouseY)));
        }
        generators.removeAll(generatorsToRemove);
        generatorsToRemove.clear();
        for (Generator generator : generators) {
            generator.update();
        }
    }

    private void updateParticles() {
        group("particles");
        particles.removeAll(particlesToRemove);
        particlesToRemove.clear();
        for (Particle particle : particles) {
            particle.update();
        }
    }

    private boolean outOfBounds(PVector pos) {
        float screenMargin = slider("death margin");
        return !isPointInRect(pos.x, pos.y, -screenMargin, -screenMargin,
                width + screenMargin, height + screenMargin);
    }

    class Generator {
        PVector pos = new PVector(), spd = new PVector();

        Generator(PVector pos) {
            this.pos = pos;
        }

        void update() {
            if (outOfBounds(pos)) {
                generatorsToRemove.add(this);
                return;
            }
            float frequence = slider("freq");
            float amp = slider("angle variation");
            float noiseAngle = amp * (1 - 2 * noise(pos.x * frequence, pos.y * frequence));
            spd = sliderXY("speed", 0, 1, 10).copy();
            spd.rotate(noiseAngle);
            pos.add(spd);
            if (toggle("debug")) {
                pg.stroke(255, 0, 0);
                pg.strokeWeight(4);
                pg.point(pos.x, pos.y);
            }
            if (frameCount % sliderInt("particle spawn modulo", 1, 10) == 0) {
                particles.add(new Particle(this, TWO_PI + QUARTER_PI, TWO_PI));
                particles.add(new Particle(this, PI - QUARTER_PI, PI));
            }
        }

    }

    class Particle {
        Generator parent;
        int frameCreated = frameCount;
        PVector pos;
        private float baseAngleStart;
        private float baseAngleEnd;
        private ArrayList<PVector> path = new ArrayList<>();

        Particle(Generator parent, float baseAngleStart, float baseAngleEnd) {
            pos = parent.pos.copy();
            this.parent = parent;
            this.baseAngleStart = baseAngleStart;
            this.baseAngleEnd = baseAngleEnd;
        }

        void update() {
            if (outOfBounds(pos)) {
                particlesToRemove.add(this);
                return;
            }
            float frameLifespan = slider("lifespan", 150);
            float lifespanNorm = constrain(norm(frameCount, frameCreated, frameCreated + frameLifespan), 0, 1);
            float easedNorm = ease(lifespanNorm, slider("ease heading"));
            if (Float.isNaN(easedNorm)) {
                easedNorm = 1;
            }
            float frequence = slider("freq", .2f);
            float amp = slider("angle variation", 1);
            float noiseAngle = amp * (1 - 2 * noise(pos.x * frequence, pos.y * frequence));
            float heading = map(easedNorm, 0, 1, baseAngleStart, baseAngleEnd);
            PVector spd = PVector.fromAngle(heading + noiseAngle).mult(slider("speed", 1));
            float prevX = pos.x;
            float prevY = pos.y;
            pos.add(spd);
            path.add(pos.copy());
            pg.strokeWeight(slider("weight", 1));
            pg.stroke(picker("stroke").clr());
            pg.noFill();
            pg.line(prevX, prevY, pos.x, pos.y);
            for (Particle other : particles) {
                float r = slider("destroy radius", 150);
                if (!other.parent.equals(this.parent)) {
                    int checkSkip = sliderInt("destroy check skip", 10);
                    int i = 0;
                    for (PVector p : other.path) {
                        if (i++ % checkSkip == 0) {
                            if (isPointInRect(p.x, p.y, pos.x - r * .5f, pos.y - r * .5f, r, r)) {
                                particlesToRemove.add(this);
                            }
                        }

                    }
                }
            }
        }
    }
}
