import applet.HotswapGuiSketch;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2019-10-29
 */
public class GridMask extends HotswapGuiSketch {
    private PGraphics pg, mask;
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("GridMask");
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
        mask = createGraphics(width, height, P2D);
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        updateAnimation();
        pg.beginDraw();
        String gridMask = "gridMask.glsl";
        uniform(gridMask).set("time", t);
        uniform(gridMask).set("maskTex", mask);
        hotFilter(gridMask, pg);
        chromaticAberrationPass(pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void updateAnimation() {
        drawMask(mask);
    }

    private void drawMask(PGraphics mask) {
        mask.beginDraw();
        mask.clear();
        mask.rectMode(CENTER);
        mask.translate(width * .5f, height * .5f);
        mask.rotate(slider("rot start", TWO_PI) + (toggle("rotate dir")?t:-t));
        mask.fill(255);
        mask.noStroke();
        float size = slider("rect size", 400);
        mask.rect(0, 0, size, size);
        mask.endDraw();
    }
}
