import applet.HotswapGuiSketch;
import processing.core.PGraphics;

public class GuiTest extends HotswapGuiSketch {
    public static void main(String[] args) {
        HotswapGuiSketch.main("GuiTest");
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
        pg.background(0);

        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
