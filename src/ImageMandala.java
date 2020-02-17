import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class ImageMandala extends KrabApplet {
    private PImage img;

    public static void main(String[] args) {
        ImageMandala.main("ImageMandala");
    }

    private PGraphics pg;

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        img = squareify(loadImage("images/film_7_clouds_by_hiura.jpg"));
    }

    private PImage squareify(PImage img) {
        int smallerDimension = min(img.width, img.height);
        return img.get(0,0,smallerDimension, smallerDimension);
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        String rectImage = "imageMandala.glsl";
        uniform(rectImage).set("img", img);
        uniform(rectImage).set("time", t);
        hotFilter(rectImage, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
