package juicygui;

import applet.HotswapGuiSketch;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-06
 */
public class Slider extends HotswapGuiSketch {

    public static void main(String[] args) {
        HotswapGuiSketch.main("juicygui.Slider");
    }

    private float sliderValue;

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
        sliderValue = slider("value", 100);
        slider("abcdefg", 0, 100, width * .5f - w * .5f, height * .5f - h * .5f, w, h);
        gui();
    }

    void slider(String name, float min, float max, float x, float y, float w, float h) {
        float handleWidth = w * slider("handle w mult", .2f);
        float handleOverflow = slider("overflow", 20);
        float handleHeight = h + handleOverflow * 2;
        float sliderHandleCenterX = map(sliderValue, min, max, x + handleWidth * .5f, x + w - handleWidth * .5f);

        pushStyle();

        stroke(255);
        strokeWeight(1);

        // left background
        rectMode(CORNERS);
        fill(slider("left bg", 0, 255, 20));
        rect(x, y, sliderHandleCenterX, y + h);

        float handleBg = slider("handle bg", 0, 255, 50);

        // right background
        fill(slider("right bg", 0, 255, 0));
        rect(sliderHandleCenterX, y, x + w, y + h); // rounded?

        // text
        fill(slider("text fill", 255, true));
        textAlign(LEFT, CENTER);
        textSize(slider("text size", 0, 80, 25));
        text(name,x+slider("text margin x", -5,  10, 5.6388893f),y + slider("text margin y", -50, 50, 13.703705f));

        // handle
        rectMode(CENTER);
        fill(handleBg);
        rect(sliderHandleCenterX, y + h * .5f, handleWidth, handleHeight);

        popStyle();
    }
}
