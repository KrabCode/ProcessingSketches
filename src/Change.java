import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-06
 */
public class Change extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Change");
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

        drawArc();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void drawArc() {
        float r0 = slider("r0", 200);
        float r1 = slider("r1", 300);
        float start = slider("a0", HALF_PI) + .5f+.5f*sin(t)*slider("start offset");
        float end = slider("a0", HALF_PI) + .5f+.5f*sin(PI+t)*slider("end offset");
        int detail = sliderInt("detail");
        pg.pushMatrix();
        pg.translate(width * .5f, height * .5f);
        pg.beginShape(TRIANGLE_STRIP);
        pg.noStroke();
        pg.fill(picker("fill").clr());
        for (int i = 0; i < detail; i++) {
            float inorm = norm(i, 0, detail);
            float a = map(inorm, 0, 1, start, end);
            pg.vertex(r0 * cos(a), r0 * sin(a));
            pg.vertex(r1 * cos(a), r1 * sin(a));
        }
        pg.endShape();
        pg.popMatrix();
    }
}
