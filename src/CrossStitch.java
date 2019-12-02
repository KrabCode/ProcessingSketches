import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class CrossStitch extends KrabApplet {
    PImage src, posterized;
    int w, h;
    int[][] pattern;
    private PGraphics pg;

    public static void main(String[] args) {
        CrossStitch.main("CrossStitch");
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
        pg.background(0);
        updateImages();
        updatePattern();
        String draw = options("source", "poster", "pattern");
        if (draw.equals("source")) {
            pg.image(src, 0, 0, width, height);
        } else if (draw.equals("poster")) {
            pg.image(posterized, 0, 0, width, height);
        } else {
            drawPattern();
        }
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void updateImages() {

    }

    private void updatePattern() {
        w = sliderInt("w");
        h = sliderInt("h");
        pattern = new int[w][h];
        colorMode(HSB, 1, 1, 1, 1);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                pattern[x][y] = color(.5f + .5f * sin(x * y + t));
            }
        }
    }

    private void drawPattern() {
        float rectSize = width / (float) w;
        for (int xi = 0; xi < w; xi++) {
            for (int yi = 0; yi < h; yi++) {
                float x = map(xi, 0, w-1, 0, width);
                float y = map(yi, 0, h-1, 0, height);
                pg.noStroke();
                pg.fill(pattern[xi][yi]);
                pg.rect(x,y, rectSize, rectSize);
            }
        }
    }
}
