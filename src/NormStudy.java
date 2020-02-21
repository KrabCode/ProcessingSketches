import applet.KrabApplet;
import processing.core.PGraphics;

public class NormStudy extends KrabApplet {
    public static void main(String[] args) {
        NormStudy.main("NormStudy");
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
        pg.background(0);
        pg.translate(0, height*.5f);
        pg.beginShape();
        pg.noFill();
        pg.stroke(255);
        pg.strokeWeight(1.99f);
        for (int x = 0; x < width; x++) {
            float start = slider("start", 200);
            float end = slider("end", 600);
            float norm = clampNorm(x, start, end);
            pg.vertex(x, norm*200);
        }
        pg.endShape();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
