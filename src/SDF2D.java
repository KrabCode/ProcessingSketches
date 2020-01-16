import applet.KrabApplet;
import processing.core.PGraphics;

public class SDF2D extends KrabApplet {
    public static void main(String[] args) {
        SDF2D.main("SDF2D");
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
        group("test");
        float slider = slider("slider");
        String sdf2D = "sdf2D.glsl";
        uniform(sdf2D).set("time", t);
        hotFilter(sdf2D, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
