import applet.HotswapGuiSketch;
import processing.core.PGraphics;

public class Sun extends HotswapGuiSketch {
    public static void main(String[] args) {
        HotswapGuiSketch.main("Sun");
    }

    private float t;
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
        recordingFrames = 360*2;
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        pg.beginDraw();
        String radialDoodle = "sun.glsl";
        uniform(radialDoodle).set("time", t);
        hotFilter(radialDoodle);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
