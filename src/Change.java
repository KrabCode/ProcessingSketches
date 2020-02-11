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
        pg.colorMode(HSB,1,1,1,1);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        animation();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void animation() {
        String change = "change.glsl";
        uniform(change).set("time", t);
        uniform(change).set("shininess", slider("shininess"));
        uniform(change).set("lightDir", sliderXYZ("light dir"));
        hotFilter(change, pg);
    }
}
