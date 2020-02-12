import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import codeanticode.glgraphics.*;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-12
 */
public class Ultrablue extends KrabApplet {
    private PGraphics pg;
    PImage rgbNoise;

    public static void main(String[] args) {
        KrabApplet.main("Ultrablue");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, GLConstants.GLGRAPHICS);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();

        rgbNoise = loadImage("noise/rgb.png");
    }

    public void draw() {
        pg.beginDraw();
//        String raymarch = "raymarch_blinn_phong.glsl";
//        uniform(raymarch).set("time", t);
//        uniform(raymarch).set("lightDir", sliderXYZ("light dir"));
//        uniform(raymarch).set("shininess", slider("shininess"));
//        hotFilter(raymarch, pg);

        GLTextureParameters textureParameters = new GLTextureParameters();
        textureParameters.wrappingU = REPEAT;
        textureParameters.wrappingV = REPEAT;

        String textureNoise = "noise/textureNoise.glsl";
        uniform(textureNoise).set("rgbNoise", rgbNoise);
        uniform(textureNoise).set("time", t);
        hotFilter(textureNoise, pg);

        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
