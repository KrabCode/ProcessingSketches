import applet.juicygui.JuicyGuiSketch;
import processing.core.PGraphics;
import processing.core.PVector;

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
        group("background");
        pg.background(sliderFloat("background", 0, 100));
        pg.rectMode(CENTER);
        group("rect");
        pg.fill(sliderFloat("fill", 100, 100));
        float size = sliderFloat("size", 50, 10);
        group("text");
        PVector translate = slider2D("translate", width*.5f, width*.5f, 100);
        pg.translate(translate.x, translate.y);
        pg.textAlign(CENTER, CENTER);
        pg.fill(sliderFloat("fill", 255, 50));
        pg.textSize(sliderFloat("textSize", 4, 50));
        pg.text(radio("hi", "world", "bejbii"), 0, 0);
        pg.rect(0, 0, size, size);
        pg.endDraw();
        image(pg, 0, 0);
        gui();
    }
}
