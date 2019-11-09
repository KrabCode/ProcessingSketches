package applet.juicygui;

import applet.HotswapGuiSketch;

@SuppressWarnings({"FieldCanBeLocal"})
public class InfiniteSlider extends HotswapGuiSketch {
    float spd = 0;
    private int white = color(255);
    private int grayLight = color(200);
    private float value = 0;
    private float precision = 1f;
    private float sliderWidth, sliderHeight, leftEdgeX, rightEdgeX, topEdgeY, botEdgeY, leftEdgeValue, rightEdgeValue;

    public static void main(String[] args) {
        HotswapGuiSketch.main("applet.juicygui.InfiniteSlider");
    }

    public void settings() {
        fullScreen(P2D);
    }

    public void setup() {

    }

    public void draw() {
        translate(width * .5f, height * .5f + slider("y", -1000, 1000));
        background(0);
        if(button("precision +")){
            precision *= .1f;
        }
        if(button("precision -")){
            precision *= 10;
        }
        if(mousePressedOutsideGui){
            float screenSpaceDelta = pmouseX-mouseX;
            float valueToScreenRatio = (precision * 2) / sliderWidth;
            float valueSpaceDelta = screenSpaceDelta * valueToScreenRatio;
            if (abs(valueSpaceDelta) > 0) {
                spd = valueSpaceDelta;
            }
        }
        spd *= slider("drag", .5f, 1);
        value += spd;
        infiniteSlider();
        gui();
    }

    private void infiniteSlider() {
        sliderWidth = slider("width", 0, 1200, 900);
        sliderHeight = slider("height", 600);
        leftEdgeX = -sliderWidth * .5f;
        rightEdgeX = sliderWidth * .5f;
        topEdgeY = -sliderHeight * .5f;
        botEdgeY = sliderHeight * .5f;
        leftEdgeValue = -precision;
        rightEdgeValue = precision;
        strokeWeight(slider("weight", 5));
        drawHorizontalLine();
        drawMarkerLines(precision * 1.0f, sliderHeight * .4f, true);
        drawMarkerLines(precision * 0.5f, sliderHeight * .2f, false);
        drawMarkerLines(precision * .05f, sliderHeight * .1f, false);
        strokeWeight(1);
        drawSelectionBox();
    }

    private void drawHorizontalLine() {
        stroke(grayLight);
        line(leftEdgeX, 0, rightEdgeX, 0);
    }

    private void drawSelectionBox() {
        stroke(white);
        noFill();
        rectMode(CENTER);
        rect(0, 0, slider("handle width", 40), sliderHeight * slider("handle height", 0, 1, .7f), slider("roundness", 20));
        fill(grayLight);
        if (toggle("long text")) {
            text(String.valueOf(value), 0, topEdgeY - 25);
        } else {
            text(value, 0, topEdgeY - 25);
        }
    }


    private void drawMarkerLines(float frequency, float markerHeight, boolean text) {
        float markerValue = leftEdgeValue - value;
        while (markerValue <= rightEdgeValue - value) {
            float moduloValue = markerValue;
            while (moduloValue > rightEdgeValue) {
                moduloValue -= precision * 2;
            }
            while (moduloValue < leftEdgeValue) {
                moduloValue += precision * 2;
            }
            float screenX = map(moduloValue, leftEdgeValue, rightEdgeValue, leftEdgeX, rightEdgeX);
            stroke(grayLight);
            line(screenX, -markerHeight * .5f, screenX, markerHeight * .5f);
            if (text) {
                float displayValue = moduloValue + value;
                String displayText = nf(displayValue, 0, 0);
                if (displayText.equals("-0")) {
                    displayText = "0";
                }
                fill(grayLight);
                textAlign(CENTER, CENTER);
                textSize(slider("text size", 100));
                text(displayText, screenX, botEdgeY + 15);
            }
            markerValue += frequency;
        }
    }
}
