import applet.KrabApplet;
import processing.core.PGraphics;

@SuppressWarnings("DuplicatedCode")
public class SharpenBlur_2 extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        SharpenBlur_2.main("SharpenBlur_2");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(800, 800, P2D);
        frameRecordingDuration *= 2;
    }

    public void draw() {
        pg.beginDraw();
        group("global");
        alphaFade(pg);
        if (frameCount < 5 || button("seed")) {
            pg.background(0);
            pg.stroke(255);
            pg.strokeWeight(3);
            int count = sliderInt("count", 40);
            for (int xi = 0; xi < count; xi++) {
                float x = map(xi, 0, count - 1, 0, pg.width);
                pg.line(x, 0, x, pg.height);
            }
            for (int yi = 0; yi < count; yi++) {
                float y = map(yi, 0, count - 1, 0, pg.width);
                pg.line(0, y, pg.width, y);
            }
        }
        group("blur sharpen");
        blurSharpenPass();
        group("move");
        displacePass(pg);
        group("vignette");
        vignettePass(pg);
        pg.endDraw();
        image(pg, 0, 0, width, height);
        brightenPass(g);
        rec(g);
        gui();
    }

    private void brightenPass(PGraphics pg){
        String brighten = "brighten.glsl";
        uniform(brighten).set("smoothstepStart", slider("brighten start", 1));
        uniform(brighten).set("smoothstepEnd", slider("brighten end", 1));
        hotFilter(brighten, pg);
    }

    private void radialMovePass(PGraphics pg) {
        String radialMovePass = "move.glsl";
        uniform(radialMovePass).set("time", t);
        uniform(radialMovePass).set("angleOffset", slider("angle offset", PI));
        uniform(radialMovePass).set("timeSpeed", slider("time speed"));
        uniform(radialMovePass).set("pixelMag", slider("pixel mag", .2f));
        hotFilter(radialMovePass, pg);
    }

    private void blurSharpenPass() {
        String bs = "blurSharpenDrawing.glsl";
        float maxSharpen = slider("max sharpen");
        float maxBlur = slider("max blur");
        uniform(bs).set("maxSharpen", maxSharpen);
        uniform(bs).set("maxBlur", maxBlur);
        uniform(bs).set("blurOrSharpen", true);
        hotFilter(bs, pg);
        uniform(bs).set("blurOrSharpen", false);
        hotFilter(bs, pg);
    }
}
