import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2020-03-13
 */
public class FractalFlame extends KrabApplet {
    ArrayList<Particle> ps = new ArrayList<>();
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("FractalFlame");
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
        background(0);
        pg.beginDraw();
        if (button("reset")) {
            pg.background(0);
            ps.clear();
        }
        alphaFade(pg);
        pg.translate(width * .5f, height * .5f);
        PVector translate = sliderXY("translate");
        pg.translate(translate.x, translate.y);
        updateParticles();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void updateParticles() {
        int intendedParticleCount = floor(slider("count", 100));
        while (ps.size() < intendedParticleCount) {
            ps.add(new Particle());
        }
        while (ps.size() > intendedParticleCount) {
            ps.remove(0);
        }
        for (Particle p : ps) {
            p.update();
        }
    }

    class Particle {
        int frameCreated = frameCount;
        PVector pos = new PVector(random(-1, 1), random(-1, 1));

        void update() {
            float random = random(1);
            if (random < .33f) {
                f0();
            } else if (random < .66f) {
                f1();
            } else {
                f2();
            }
            if (frameCreated + 20 < frameCount) {
                display();
            }
        }

        void display() {
            pg.stroke(picker("stroke").clr());
            pg.strokeWeight(slider("weight", 1));
            pg.point(map(pos.x, -1, 1, -width / 2f, width / 2f),
                    map(pos.y, -1, 1, -height / 2f, height / 2f));
        }

        void f0() {
            pos.x = pos.x / 2.f;
            pos.y = pos.y / 2.f;
        }

        void f1() {
            pos.x = (pos.x + 1) / 2.f;
            pos.y = pos.y / 2f;
        }

        void f2() {
            pos.x = pos.x / 2f;
            pos.y = (pos.y + 1) / 2f;
        }

    }

}

