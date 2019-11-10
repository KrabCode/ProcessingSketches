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
        pg.noSmooth();
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        group("background");
        pg.background(sliderFloat("background", 0, 100));
        pg.rectMode(CENTER);

        PVector translate = new PVector(width*.5f, width*.5f);
        pg.translate(translate.x, translate.y);

        group("rect");
        pg.stroke(sliderFloat("stroke", 255, 100));
        pg.strokeWeight(sliderFloat("weight", 2, 10));
        pg.fill(sliderFloat("fill", 100, 100));
        float size = sliderFloat("size", 50, 100);
        pg.rect(0, 0, size, size);

        group("text");
//        PVector translate = slider2D("translate", width*.5f, width*.5f, 100);
        pg.textAlign(CENTER, CENTER);
        pg.fill(sliderFloat("fill", 255, 100));
        pg.textSize(floor(sliderFloat("size", textSize*2, 100)));
        pg.text(radio( "hello", "world!"), 0, 0);

        pg.endDraw();
        image(pg, 0, 0);
        gui();
    }
}
