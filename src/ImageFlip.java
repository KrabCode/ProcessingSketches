import applet.HotswapGuiSketch;
import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-18
 */
public class ImageFlip extends KrabApplet {
    private PGraphics pg;
    private PImage img;

    public static void main(String[] args) {
        KrabApplet.main("ImageFlip");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        img = squareify(loadImage("images/film_7_clouds_by_hiura.jpg"));
    }

    public void draw() {
        pg.beginDraw();
        String imageFlip = "imageFlip.glsl";
        uniform(imageFlip).set("time", t);
        uniform(imageFlip).set("img", img);
        hotFilter(imageFlip, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private PImage squareify(PImage img) {
        int smallerDimension = min(img.width, img.height);
        return img.get(0,0,smallerDimension, smallerDimension);
    }
}
