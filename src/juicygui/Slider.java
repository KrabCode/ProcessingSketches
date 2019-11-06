package juicygui;

import applet.HotswapGuiSketch;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-06
 */
public class Slider extends HotswapGuiSketch {

    float sliderValue = 50;

    public static void main(String[] args) {
        HotswapGuiSketch.main("juicygui.Slider");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
    }

    public void draw() {
        background(0);
        float w = slider("w", 400);
        float h = w * slider("h multiplier", 0.35f);
        slider("abcdefg", 0, 100, width * .5f - w * .5f, height * .5f - h * .5f, w, h);
        gui();
    }

    void slider(String name, float min, float max, float x, float y, float w, float h) {
        fill(0);
        stroke(255);
        strokeWeight(1);
        rect(x, y, w, h, 10); // rounded?

        float handleWidth = w * slider("handle w mult", .1f);
        float handleOverflow = 2;
        float sliderHandleCenter = map(sliderValue, min, max, x, x + w);

        fill(50);
        stroke(255);
        strokeWeight(1);
        rect(sliderHandleCenter - handleWidth * .5f, y - handleOverflow, handleWidth, h + handleOverflow * 2);


    }
}
