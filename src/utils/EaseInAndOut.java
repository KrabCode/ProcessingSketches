package utils;

import applet.KrabApplet;
import processing.core.PGraphics;

public class EaseInAndOut extends KrabApplet {
    public static void main(String[] args) {
        EaseInAndOut.main("utils.EaseInAndOut");
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
        pg.beginShape();
        pg.stroke(255);
        pg.strokeWeight(2);
        pg.noFill();
        for (int x = 0; x < width; x+=5) {
            float y = height*.5f+slider("amp", 100)
                    * easeInAndOut(x, slider("w"), slider("tw"), slider("c", width*.5f), slider("easing", 1));
            pg.vertex(x,y);
        }
        pg.endShape();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
