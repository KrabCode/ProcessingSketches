import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.ArrayList;

public class Seasons extends HotswapGuiSketch {
    public static void main(String[] args) {
        HotswapGuiSketch.main("Seasons");
    }

    private float t;
    private PGraphics pg;
    private String seasons = "seasons.glsl";
    ArrayList<PImage> seasonImages = new ArrayList<>();

    public void settings() {
        size(750, 750, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        for (int i = 0; i < 4; i++) {
            PImage image = loadImage("images/seasons/" + i + ".jpg");
            seasonImages.add(image.get(0, 0, 750, 750));
        }
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        pg.beginDraw();
        pg.background(0);
        uniform(seasons).set("time", t);
        for (int i = 0; i < 4; i++) {
            uniform(seasons).set("img_" + i, seasonImages.get(i));
        }
        hotFilter(seasons);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui(false);
    }
}
