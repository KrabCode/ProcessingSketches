import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class Mandoloid extends KrabApplet {
    public static void main(String[] args) {
        Mandoloid.main("Mandoloid");
    }

    private PGraphics pg;
    private PImage img;

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        surface.setLocation(1920-820,20);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        img = loadImage("images/mandala_by_pokku.jpg");
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        String mandalaRotator = "mandalaRotator.glsl";
        uniform(mandalaRotator).set("img", img);
        uniform(mandalaRotator).set("time", t);
        hotFilter(mandalaRotator, pg);
//        pg.stroke(255,0,0);
//        pg.line(width/2,0,width/2, height);
//        pg.line(0, height/2, width, height/2);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
