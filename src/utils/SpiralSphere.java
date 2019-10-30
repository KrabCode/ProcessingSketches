package utils;

import applet.HotswapGuiSketch;
import processing.core.PGraphics;

public class SpiralSphere extends HotswapGuiSketch {
    public static void main(String[] args) {
        HotswapGuiSketch.main("utils.SpiralSphere");
    }

    private float t;
    private PGraphics pg;

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        t += radians(slider("t", 0, 1, .15f));
        pg.beginDraw();
        pg.background(0);
        pg.translate(width*.5f, height*.5f);
        pg.rotateX(t*1);
        pg.rotateY(t*2);
        pg.rotateZ(t*3);
        spiralSphere(pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void spiralSphere(PGraphics pg) {
        pg.beginShape(POINTS);
        pg.stroke(255);
        pg.strokeWeight(slider("weight", 2));
        pg.noFill();
        float N = slider("count", 3000);
        float s  = 3.6f/sqrt(N);
        float dz = 2.0f/N;
        float lon = 0;
        float  z = 1 - dz/2;
        float scl = slider("scl", 500);
        for (int k = 0; k < N; k++) {
            float r = sqrt(1-z*z);
            pg.vertex(cos(lon)*r*scl, sin(lon)*r*scl, z*scl);
            z    = z - dz;
            lon = lon + s/r;
        }
        pg.endShape();
    }
}
