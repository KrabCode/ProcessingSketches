import applet.KrabApplet;
import processing.core.PGraphics;

public class SharpenBlur extends KrabApplet {
    public static void main(String[] args) {
        SharpenBlur.main("SharpenBlur");
    }

    private PGraphics pg;

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(1080,1080, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        frameRecordingDuration *= 4;
    }

    public void draw() {
        pg.beginDraw();
        if(frameCount < 5 || button("seed")){
            pg.background(0);
            pg.noStroke();
            pg.fill(255);
            pg.ellipse(pg.width*.5f, pg.height*.5f, 5, 5);
        }
        group("blur sharpen");
        blurSharpenPass();
        group("radial move");
        radialMovePass(pg);
        group("vignette");
        vignettePass(pg);
        pg.endDraw();
        image(pg, 0, 0, width, height);
        rec(pg);
        gui();
    }

    private void radialMovePass(PGraphics pg) {
        String radialMovePass = "radialMove.glsl";
        uniform(radialMovePass).set("time", t);
        uniform(radialMovePass).set("baseAngle", slider("base angle", PI));
        uniform(radialMovePass).set("timeSpeed", slider("time speed"));
        uniform(radialMovePass).set("octaves", sliderInt("octaves"));
        uniform(radialMovePass).set("baseAmp", slider("amp"));
        uniform(radialMovePass).set("baseFrequency", slider("freq"));
        uniform(radialMovePass).set("ampMult", slider("amp mult"));
        uniform(radialMovePass).set("freqMult", slider("freq mult"));
        uniform(radialMovePass).set("pixelMag", slider("pixel mag"));
        hotFilter(radialMovePass, pg);
    }

    private void blurSharpenPass() {
        String bs = "blurSharpen.glsl";
        uniform(bs).set("baseSharpen", slider("sharpen"));
        uniform(bs).set("offSharpen", slider("off sharpen"));
        uniform(bs).set("baseBlur", slider("blur"));
        uniform(bs).set("offBlur", slider("off blur"));
        uniform(bs).set("blurOrSharpen", true);
        hotFilter(bs, pg);
        uniform(bs).set("blurOrSharpen", false);
        hotFilter(bs, pg);
    }
}
