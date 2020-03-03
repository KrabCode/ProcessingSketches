package utils;

import applet.KrabApplet;
import processing.core.PGraphics;

public class GradientPicker extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        GradientPicker.main("utils.GradientPicker");
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
        pg.background(0);
        colorPalettePass();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void colorPalettePass() {
        String colorPaletteShader = "gradient.glsl";
        int colorCount = sliderInt("color count", 10);
        for (int i = 0; i < colorCount; i++) {
            HSBA color = picker(i + "");
            uniform(colorPaletteShader).set("hsba_" + i, color.hue(), color.sat(), color.br(), color.alpha());
        }
        uniform(colorPaletteShader).set("colorCount", colorCount);
        uniform(colorPaletteShader).set("time", t);
        hotFilter(colorPaletteShader, pg);
    }
}
