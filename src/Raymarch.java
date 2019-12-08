import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2019-12-07
 */
public class Raymarch extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Raymarch");
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
        frameRecordingDuration *= 3;
    }

    public void draw() {
        pg.beginDraw();
        rayMarchPass(pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
