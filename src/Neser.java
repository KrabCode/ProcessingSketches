import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Created by Jakub 'Krab' Rak on 2020-03-14
 */
public class Neser extends KrabApplet {
    private PGraphics pg;
    PImage neser;

    public static void main(String[] args) {
        KrabApplet.main("Neser");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        neser = loadImage("images/neser.jpg");
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        background(255);
        pg.beginDraw();
        pg.background(255);
        pg.imageMode(CENTER);
        pg.translate(width*.5f, height*.5f);
        pg.scale(slider("scale", 1));
        pg.image(neser, 0, 0);
        String neserShader = "neser.glsl";
        uniform(neserShader).set("time", t);
        uniformColorPalette(neserShader);
        hotFilter(neserShader, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
