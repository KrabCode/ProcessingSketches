import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-06
 */
public class Change extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Change");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.endDraw();
        frameRecordingDuration *= 2;
//        frameRecordingStarted = 1;
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        pg.translate(width * .5f, height * .5f);
        pg.rotate(-HALF_PI);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }


}
