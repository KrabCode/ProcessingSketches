import applet.HotswapGuiSketch;
import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-14
 */
public class GradientLines extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("GradientLines");
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
        frameRecordingDuration *= 2.;
    }

    public void draw() {
        pg.beginDraw();
        String gradientLines = "gradientLines.glsl";
        uniform(gradientLines).set("time", t);
        hotFilter(gradientLines, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
