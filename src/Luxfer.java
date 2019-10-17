import applet.HotswapGuiSketch;
import processing.core.PGraphics;

public class Luxfer extends HotswapGuiSketch {
    public static void main(String[] args) {
        HotswapGuiSketch.main("Luxfer");
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
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        pg.beginDraw();
        String causticsShader = "luxfer.glsl";
        uniform(causticsShader).set("time", t);
        hotFilter(causticsShader);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui(false);
    }
}
