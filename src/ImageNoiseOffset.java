import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PImage;

public class ImageNoiseOffset extends HotswapGuiSketch {
    PGraphics pg;
    PImage image;
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("ImageNoiseOffset");
    }

    public void settings() {
        image = loadImage("images\\0.jpg");
        size(image.width, image.height, P2D);
    }

    public void setup() {
        pg = createGraphics(width, height, P2D);
    }

    public void draw() {
        t += radians(slider("t"));
        tint(255, slider("alpha")*255);
        image(image, 0, 0, width, height);
        noiseOffsetPass(g, t);
        rec();
        gui();
    }
}
