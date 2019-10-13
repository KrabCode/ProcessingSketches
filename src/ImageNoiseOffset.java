import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PImage;

public class ImageNoiseOffset extends HotswapGuiSketch {
    PGraphics pg;
    PImage jesus;
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("ImageNoiseOffset");
    }

    public void settings() {
        size(720,972, P2D);
    }

    public void setup() {
        pg = createGraphics(width, height, P2D);
        jesus = loadImage("images\\jesus.jpg");
    }

    public void draw() {
        t += radians(slider("t"));
        tint(255, slider("alpha")*255);
        image(jesus, 0, 0, width, height);
        noiseOffsetPass(g, t);
        rec();
        gui();
    }
}
