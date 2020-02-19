import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class NoiseTextureTest extends KrabApplet {
    private PImage noise;

    public static void main(String[] args) {
        NoiseTextureTest.main("NoiseTextureTest");
    }

    private PGraphics pg;

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        noise = loadImage("noise/50.jpg");
        frameRecordingDuration *= 2;
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        String noiseTex = "noise/noiseTex.glsl";
        uniform(noiseTex).set("time", t);
        uniform(noiseTex).set("noise", noise);
        hotFilter(noiseTex, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
