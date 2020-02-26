import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-12
 */
public class RaymarchBlinnPhong extends KrabApplet {
    private PGraphics pg;
    private PImage noise;

    public static void main(String[] args) {
        KrabApplet.main("RaymarchBlinnPhong");
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
        noise = loadImage("noise/50.jpg");
    }

    public void draw() {
        pg.beginDraw();
        String raymarch = "raymarch_blinn_phong.glsl";
        uniform(raymarch).set("time", t);
        uniform(raymarch).set("lightDir", sliderXYZ("light dir"));
        uniform(raymarch).set("shininess", slider("shininess"));
        uniform(raymarch).set("noise", noise);
        hotFilter(raymarch, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
