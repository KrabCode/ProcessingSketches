import applet.KrabApplet;
import processing.core.PGraphics;

public class GradientMandala extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        GradientMandala.main("GradientMandala");
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
        frameRecordingDuration *= 4;
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
        String mandala = "gradientMandala.glsl";
        uniform(mandala).set("time", t);
        uniformColorPalette(mandala);
        hotFilter(mandala, pg);
    }


}
