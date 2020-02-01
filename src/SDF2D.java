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
        String logSpiralShader = "sdf2d.glsl";
        uniform(logSpiralShader).set("time", t);
        hotFilter(logSpiralShader, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
