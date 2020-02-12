import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-06
 */
public class WaterRipple1D extends KrabApplet {
    ArrayList<Float> waterBufferA = new ArrayList<Float>();
    ArrayList<Float> waterBufferB = new ArrayList<Float>();
    ArrayList<Float> waterBufferTemp = new ArrayList<Float>();
    ArrayList<Particle> particles = new ArrayList<Particle>();
    ArrayList<Particle> particlesToRemove = new ArrayList<Particle>();
    int waterSize;
    PVector center;
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("WaterRipple1D");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.endDraw();
        center = new PVector(width * .5f, height * .5f);
        initWater();
    }

    public void draw() {
        pg.beginDraw();
        pg.translate(0, height * .5f);
        alphaFade(pg);
        int previousWaterSize = waterSize;
        waterSize = sliderInt("water size", width);
        if (waterSize != previousWaterSize) {
            initWater();
        }
        updateWater();
        drawWater();
        updateParticles();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void initWater() {
        waterBufferA.clear();
        waterBufferB.clear();
        waterBufferTemp.clear();
        for (int i = 0; i < waterSize; i++) {
            waterBufferA.add(0f);
            waterBufferB.add(0f);
            waterBufferTemp.add(0f);
        }
    }

    private void updateWater() {
        for (int i = 1; i < waterSize - 1; i++) {
            float val = (waterBufferA.get(i - 1) + waterBufferA.get(i + 1)) / slider("neighbour divide", 2)
                    - waterBufferB.get(i);
            val *= slider("damp", .9f);
            waterBufferB.set(i, val);
        }
        waterBufferTemp.clear();
        waterBufferTemp.addAll(waterBufferA);
        waterBufferA.clear();
        waterBufferA.addAll(waterBufferB);
        waterBufferB.clear();
        waterBufferB.addAll(waterBufferTemp);
    }

    private void drawWater() {
        pg.beginShape(TRIANGLE_STRIP);
        pg.noFill();
        pg.noStroke();
        HSBA ceiling = picker("fill ceiling");
        HSBA floor = picker("fill floor");
        for (int i = 0; i < waterBufferB.size(); i++) {
            float x = map(i, 0, waterBufferB.size() - 1, -10, width + 10);
            float y = waterBufferB.get(i);
            pg.fill(ceiling.clr());
            pg.vertex(x, y);
            pg.fill(floor.clr());
            pg.vertex(x, height);
        }
        pg.endShape();
    }

    void updateParticles() {
        int intendedCount = sliderInt("particle count", 100);
        if (button("reset particles")) {
            particles.clear();
        }
        while (particles.size() < intendedCount) {
            particles.add(new Particle());
        }
        while (particles.size() > intendedCount) {
            particles.remove(0);
        }
        for (Particle p : particles) {
            p.update();
        }
        particles.removeAll(particlesToRemove);
        particlesToRemove.clear();
    }

    class Particle {
        PVector pos;
        float weightOffset = random(1);

        Particle() {
            pos = new PVector(random(-width, width * 2), -height * .5f - random(height * .5f));
        }

        void update() {
            PVector prev = pos.copy();
            PVector spd = sliderXY("speed", 0, .5f, 1);
            pos.add(spd);
            int waterLevelBelowIndex = constrain(floor(map(pos.x, 0, width, 0, waterSize)), 0, waterSize - 1);
            float waterLevelBelow = waterBufferA.get(waterLevelBelowIndex);
            if (pos.y > waterLevelBelow) {
                waterBufferA.set(waterLevelBelowIndex, slider("impact", 10));
                particlesToRemove.add(this);
            }
            pg.noStroke();
            pg.stroke(1);
            pg.strokeWeight(slider("particle weight") + weightOffset * slider("weight offset"));
            pg.line(pos.x, pos.y, prev.x, prev.y);
        }
    }
}
