import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-01-15
 */
public class Sidewalk extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Sidewalk");
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
        pg.background(0);
        String sidewalk = "sidewalk.glsl";
        uniform(sidewalk).set("time", t);
        hotFilter(sidewalk, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
