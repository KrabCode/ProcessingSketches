package unfinished;

import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Created by Jakub 'Krab' Rak on 2019-12-06
 */
public class UnitedNations extends KrabApplet {
    private PGraphics pg;
    PImage un;

    public static void main(String[] args) {
        KrabApplet.main("unfinished.UnitedNations");
    }

    public void settings() {
        size(1280,1089, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(1280,1089, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        un = loadImage("images/un.png");
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        pg.pushMatrix();
        pg.imageMode(CENTER);
        pg.translate(pg.width*.5f, pg.height*.5f);
        pg.image(un, 0, 0);
        pg.popMatrix();
        pg.endDraw();
        image(pg, 0, 0, width, height);
        rec(pg);
        gui();
    }
}
