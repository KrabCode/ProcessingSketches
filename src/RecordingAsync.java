import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Created by Jakub 'Krab' Rak on 2020-03-09
 */
public class RecordingAsync extends KrabApplet {

    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("RecordingAsync");
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
        pg.strokeWeight(3);
        pg.stroke(255);
        pg.noFill();
        pg.translate(width*.5f, height*.5f);
        PVector offset = new PVector(slider("radius", 50), 0);
        offset.rotate(t);
        float size = slider("size", 60);
        pg.ellipse(offset.x, offset.y, size, size);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }


}
