package inktober;

import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PShape;

public class Mindless extends HotswapGuiSketch {

    PGraphics main;
    PGraphics[] planes = new PGraphics[2];
    PShape plane;

    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("inktober.Mindless");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        main = createGraphics(width, height, P3D);
        for (int i = 0; i < planes.length; i++) {
            planes[i] = createGraphics(width*3, height*3, P2D);
        }
        rectMode(CENTER);
        plane = createShape(RECT, 0, 0, width*3, height*3);
    }

    public void draw() {
        t += radians(slider("t"));
        main.beginDraw();
        alphaFade(main);
        String planeFilter = "mindless.glsl";
        uniform(planeFilter).set("time", t);

        for (int i = 0; i < planes.length; i++) {
            PGraphics planeGraphics = planes[i];
            planeGraphics.beginDraw();
            hotFilter(planeFilter, planeGraphics);
            planeGraphics.endDraw();

            float sign = i > 0 ? -1 : 1;
            plane.setTexture(planeGraphics);
            main.pushMatrix();
            main.translate(width*.5f, height*.5f+sign*slider("plane y", 500));
            main.rotateX(HALF_PI+sign*slider("rotation", -QUARTER_PI, QUARTER_PI, -.22f));
            main.translate(0, -slider("y after", height));
            main.shape(plane);
            main.popMatrix();
        }

        main.endDraw();
        image(main, 0, 0, width, height);
        rec(main);
        gui();
    }
}
