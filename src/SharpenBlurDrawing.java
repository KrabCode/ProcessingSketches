import applet.KrabApplet;
import processing.core.PGraphics;

@SuppressWarnings("DuplicatedCode")
public class SharpenBlurDrawing extends KrabApplet {
    public static void main(String[] args) {
        SharpenBlurDrawing.main("SharpenBlurDrawing");
    }

    private PGraphics pg;

    public void settings() {
        size(1000,1000, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(1000,1000, P2D);
        frameRecordingDuration *= 2;
    }

    public void draw() {
        pg.beginDraw();
        group("global");
        if(frameCount < 5 || button("seed")){
            pg.background(0);
            pg.noStroke();
            pg.fill(255);
            pg.ellipse(pg.width*.5f, pg.height*.5f, 20, 20);
        }
        group("blur sharpen");
        blurSharpenPass();
        group("move");
        radialMovePass(pg);
        group("vignette");
        vignettePass(pg);
        pg.endDraw();
        image(pg, 0, 0, width, height);
        rec(g);
        gui();
    }

    private void radialMovePass(PGraphics pg) {
        String radialMovePass = "move.glsl";
        uniform(radialMovePass).set("time", t);
        uniform(radialMovePass).set("angleOffset", slider("angle offset", PI));
        uniform(radialMovePass).set("timeSpeed", slider("time speed"));
        uniform(radialMovePass).set("octaves", sliderInt("octaves", 1));
        uniform(radialMovePass).set("baseAmp", slider("amp", 1f));
        uniform(radialMovePass).set("baseFrequency", slider("freq", .1f));
        uniform(radialMovePass).set("ampMult", slider("amp mult", .5f));
        uniform(radialMovePass).set("freqMult", slider("freq mult", 2));
        uniform(radialMovePass).set("pixelMag", slider("pixel mag", .2f));
        hotFilter(radialMovePass, pg);
    }

    private void blurSharpenPass() {
        String bs = "blurSharpenDrawing.glsl";
        float maxSharpen = slider("max sharpen");
        float sharpenFreq = slider("sharpen freq");
        float sharpenAmp = slider("sharpen amp");
        float maxBlur = slider("max blur");
        float blurFreq = slider("blur freq");
        float blurAmp = slider("blur amp");
        uniform(bs).set("maxSharpen", maxSharpen+sharpenAmp*sin(t*sharpenFreq));
        uniform(bs).set("maxBlur", maxBlur+blurAmp*sin(t*blurFreq));
        uniform(bs).set("blurOrSharpen", true);
        hotFilter(bs, pg);
        uniform(bs).set("blurOrSharpen", false);
        hotFilter(bs, pg);
    }
}
