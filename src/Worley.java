import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-01-20
 */
public class Worley extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Worley");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        frameRecordingDuration *= 3;
        surface.setAlwaysOnTop(true);
        surface.setLocation(1920-820,20);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        String worley = "worley2.glsl";
        uniform(worley).set("time", t);
        hotFilter(worley, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
