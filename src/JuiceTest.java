import applet.juicygui.JuicyGuiSketch;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-09
 */
public class JuiceTest extends JuicyGuiSketch {
    private PGraphics pg;
    private float t;

    public static void main(String[] args) {
        JuicyGuiSketch.main("JuiceTest");
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
        pg.background(100);
        pg.stroke(255, 0, 0);
        pg.fill(0);
        pg.rectMode(CENTER);
        group("rect");
        float size = sliderInt("size", 50, 10);
        pg.rect(width*.5f, height*.5f, size, size);
        pg.endDraw();
        image(pg, 0, 0);
        gui();
    }
}
