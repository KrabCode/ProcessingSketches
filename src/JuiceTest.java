import applet.juicygui.JuicyGuiSketch;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-09
 */
public class JuiceTest extends JuicyGuiSketch {
    private PGraphics pg;

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
        if (toggle("redraw", true) | button("reset once")) {
            pg.background(picker("fill", 0, 0, 0));
            pg.rectMode(CENTER);
        }
        group("matrix");
        PVector translate = sliderXY("translate", 0, 0, 1000);
        pg.translate(width * .5f + translate.x, height * .5f + translate.y);
        pg.rotate(slider("rotation", 0, 10));
        group("shape");
        pg.fill(picker("fill", .5f, 1, .5f));
        pg.stroke(picker("stroke", 0, 0, .8f));
        pg.strokeWeight(slider("weight", 2, 100));
        float size = slider("size", 150, 1000);
        pg.pushMatrix();
        pg.translate(-size * .5f, -size * .5f);
        pg.beginShape();
        pg.vertex(0, 0);
        pg.vertex(size, 0);
        pg.vertex(size, size);
        pg.vertex(0, size);
        pg.endShape(CLOSE);
        pg.popMatrix();
        pg.endDraw();
        image(pg, 0, 0);
        gui();
    }
}
