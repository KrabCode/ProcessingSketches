import applet.KrabApplet;
import processing.core.PGraphics;

public class MesmerizingColorWheel extends KrabApplet {
    public static void main(String[] args) {
        MesmerizingColorWheel.main("MesmerizingColorWheel");
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
        String colorWheel = "colorWheel.glsl";
        uniform(colorWheel).set("time", t);
        hotFilter(colorWheel, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
