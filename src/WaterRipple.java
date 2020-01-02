import applet.KrabApplet;
import processing.core.PGraphics;

public class WaterRipple extends KrabApplet {
    String cellular = "waterRipple.glsl";

    PGraphics pg;
    PGraphics a;
    PGraphics temp;


    public static void main(String[] args) {
        KrabApplet.main("WaterRipple");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        surface.setLocation(1920 - 820, 20);
        pg = createGraphics(800, 800, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        a = createGraphics(800, 800, P2D);
        a.beginDraw();
        a.background(0);
        a.endDraw();
        temp = createGraphics(800, 800, P2D);
        temp.beginDraw();
        temp.background(0);
        temp.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        uniform(cellular).set("damp", slider("damp", .99f));
        uniform(cellular).set("a", a);
        uniform(cellular).set("b", pg);
        hotFilter(cellular, pg);
        drawToPg();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        updateBuffers();
        gui();
    }

    private void drawToPg() {
        float baseR = slider("r", 10);
        int count = sliderInt("count", 10);
        float rAmp = slider("r amp", 1);
        float rFreq = slider("r freq", 1);
        pg.translate(width * .5f, height * .5f);
        for (int n = 0; n < 2; n++) {
            pg.rotate(n % 2 == 0 ? t : -t);
            pg.noStroke();
            pg.fill(255);
            for (int i = 0; i < count; i++) {
                float inorm = norm(i, 0, count - 1);
                float a = inorm * TAU;
                float r = baseR + rAmp * sin((n % 2 == 0 ? PI : 0) + t * rFreq);
                float size = slider("size");
                pg.ellipse(r * cos(a), r * sin(a), size, size);
            }
        }

    }

    private void updateBuffers() {
        temp.beginDraw();
        temp.image(a, 0, 0);
        temp.endDraw();

        a.beginDraw();
        a.image(pg, 0, 0);
        a.endDraw();

        pg.beginDraw();
        pg.image(temp, 0, 0);
        pg.endDraw();
    }
}
