import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-29
 */
public class Waves2 extends KrabApplet {
    ArrayList<Particle> particles = new ArrayList<Particle>();
    private PGraphics pg;
    private int gridSize;

    public static void main(String[] args) {
        KrabApplet.main("Waves2");
    }

    public void settings() {
//        size(800, 800, P3D);
        fullScreen(P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        frameRecordingDuration *= 4;
    }

    public void draw() {
        pg.beginDraw();
        alphaFade(pg);
        PVector translate = sliderXYZ("translate");
        PVector rotate = sliderXYZ("rotate").add(sliderXYZ("rotate speed"));
        pg.translate(width * .5f + translate.x, height * .5f + translate.y, translate.z);
        pg.rotateX(rotate.x);
        pg.rotateY(rotate.y);
        pg.rotateZ(rotate.z);
        splitPass(pg);
        updateParticles();
        pg.endDraw();
        background(0);
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    void updateParticles() {
        int intendedGridSize = sliderInt("count");
        if (intendedGridSize != gridSize) {
            gridSize = intendedGridSize;
            resetGrid();
        }
        for (Particle p : particles) {
            p.update();
        }
    }

    private void resetGrid() {
        particles.clear();
        for (int xi = 0; xi <= gridSize; xi++) {
            for (int yi = 0; yi <= gridSize; yi++) {
                particles.add(new Particle(xi, yi));
            }
        }
    }

    class Particle {
        int xi, yi;
        float satOffset = random(-1, 1);

        Particle(int xi, int yi) {
            this.xi = xi;
            this.yi = yi;
        }

        void update() {
            float gridScale = slider("grid scale");
            float x = map(xi, 0, gridSize, -gridScale, gridScale);
            float y = map(yi, 0, gridSize, -gridScale, gridScale);
            float d = dist(x, y, 0, 0);
            if (d > gridScale) {
                return;
            }
            float z = slider("sin mag") * sin(d * slider("dist sin freq") - t * slider("time freq"));
            HSBA hsba = picker("stroke");
            pg.colorMode(HSB, 1, 1, 1, 1);
            pg.stroke(hueModulo(
                    hsba.hue() + z * slider("hue z")),
                    hsba.sat() + satOffset * slider("sat offset"),
                    hsba.br(),
                    hsba.alpha());
            pg.strokeWeight(slider("weight"));
            pg.point(x, y, z);
        }
    }
}
