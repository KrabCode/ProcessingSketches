import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-12
 */
public class RaymarchBlinnPhong extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("RaymarchBlinnPhong");
    }

    public void settings() {
//        fullScreen(P2D, 2);
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
        String raymarch = "raymarch_blinn_phong.glsl";
        uniform(raymarch).set("time", t + slider("time"));
        uniform(raymarch).set("lightDirection", sliderXYZ("light dir"));
        uniform(raymarch).set("shininess", slider("shininess"));
        hotFilter(raymarch, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
