import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-01-31
 */
public class Raymarch_2 extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Raymarch_2");
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
        pg.beginDraw();
        String raymarch2 = "raymarch_1.glsl";
        uniform(raymarch2).set("time", t);
        hotFilter(raymarch2);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
