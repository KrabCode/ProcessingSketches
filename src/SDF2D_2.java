import applet.KrabApplet;
import processing.core.PGraphics;

public class SDF2D_2 extends KrabApplet {
    public static void main(String[] args) {
        SDF2D_2.main("SDF2D_2");
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
        String logSpiralShader = "sdf2d_2.glsl";
        uniform(logSpiralShader).set("time", t);
        hotFilter(logSpiralShader, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
