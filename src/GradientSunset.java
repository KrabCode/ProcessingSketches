import applet.KrabApplet;
import processing.core.PGraphics;

public class GradientSunset extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        GradientSunset.main("GradientSunset");
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
        frameRecordingDuration *= 2;
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        mandalaPass();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void mandalaPass() {
        String mandala = "gradientSunset.glsl";
        uniform(mandala).set("time", t);
        uniformColorPalette(mandala);
        hotFilter(mandala, pg);
    }


}
