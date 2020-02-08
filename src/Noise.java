import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-08
 */
public class Noise extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Noise");
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
        String noise = "noise/McGuireNoise.glsl";
        uniform(noise).set("time", t);
        hotFilter(noise, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
