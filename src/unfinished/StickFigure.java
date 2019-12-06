package unfinished;

import applet.KrabApplet;
import processing.core.PGraphics;

public class StickFigure extends KrabApplet {
    // watch https://www.youtube.com/watch?v=WHn2ouKKSfs

    public static void main(String[] args) {
        StickFigure.main("unfinished.StickFigure");
    }

    private PGraphics pg;

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
        pg.beginDraw();
        pg.background(0);

        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
