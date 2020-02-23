import applet.HotswapGuiSketch;
import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-23
 */
public class YoungMedium extends KrabApplet {
    private PGraphics pg;
    private PImage img;

    public static void main(String[] args) {
        KrabApplet.main("YoungMedium");
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
        img = loadImage("images/0.jpg");
    }

    public void draw() {
        pg.beginDraw();
        String imageFeedback = "imageFeedback.glsl";
        uniform(imageFeedback).set("img", img);
        uniform(imageFeedback).set("time", t);
        hotFilter(imageFeedback, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
