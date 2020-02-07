import applet.KrabApplet;
import processing.core.PGraphics;

public class MoveGrid extends KrabApplet {
    public static void main(String[] args) {
        MoveGrid.main("MoveGrid");
    }

    private PGraphics pg;

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
        alphaFade(pg);
        pg.stroke(255);
        pg.strokeWeight(3);
        int count = sliderInt("count", 40);
        for (int xi = 0; xi < count; xi++) {
            float x = map(xi, 0, count-1, 0, width);
            pg.line(x, 0, x, height);
        }
        for (int yi = 0; yi < count; yi++) {
            float y = map(yi, 0, count-1, 0, width);
            pg.line(0, y, width, y);
        }
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}