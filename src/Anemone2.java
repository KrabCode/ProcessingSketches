import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-26
 */
public class Anemone2 extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Anemone2");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        group("main");
        pg.beginDraw();
        alphaFade(pg);
        splitPass(pg);
        PVector pos = sliderXYZ("translate");
        PVector rot = sliderXYZ("rotate");
        pg.translate(pos.x, pos.y, pos.z);
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
