import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-12
 */
public class Ultrablue extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Ultrablue");
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
    }

    public void draw() {
        pg.beginDraw();
        String raymarch = "raymarch_blinn_phong.glsl";
        uniform(raymarch).set("time", t);
        uniform(raymarch).set("lightDir", sliderXYZ("light dir"));
        uniform(raymarch).set("shininess", slider("shininess"));
        hotFilter(raymarch, pg);

//        String textureNoise = "noise/iqNoise.glsl";
//        uniform(textureNoise).set("time", t);
//        hotFilter(textureNoise, pg);

        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
