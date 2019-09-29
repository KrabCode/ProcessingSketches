import applet.HotswapGuiSketch;
import processing.core.PGraphics;

public class RadialFeedbackLoop extends HotswapGuiSketch {
    private PGraphics pg;
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("RadialFeedbackLoop");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
    }

    public void draw() {
        t += radians(slider("t"));
        pg.beginDraw();
        radialFilter(pg);
        rgbSplitPass(pg);
        pg.endDraw();
        image(pg, 0, 0, width, height);
        rec(pg);
        gui();
    }

    void radialFilter(PGraphics pg){
        String radialFilter = "radial.glsl";
        uniform(radialFilter).set("time", t);
        uniform(radialFilter).set("mag", slider("mag", 0,.01f, .001f));
        uniform(radialFilter).set("mixAmt", slider("mix", 0,1,1));
        hotFilter(radialFilter, pg);
    }
}
