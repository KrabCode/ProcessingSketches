package inktober;

import applet.HotswapGuiSketch;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2019-10-10T19:40
 */
public class Pattern extends HotswapGuiSketch {
    private PGraphics pg;
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("inktober.Pattern");
    }

    public void settings() {
        size(800, 800, P2D);
//         fullScreen(P2D, 2);
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
        String pattern = "pattern.glsl";
        uniform(pattern).set("time", t);
        hotFilter(pattern, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui(false);
    }
}
