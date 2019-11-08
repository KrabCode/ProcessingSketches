package juicygui;

import applet.HotswapGuiSketch;

@SuppressWarnings({"FieldCanBeLocal"})
public class InfiniteSlider extends HotswapGuiSketch {
    public static void main(String[] args) {
        HotswapGuiSketch.main("juicygui.InfiniteSlider");
    }


    public void settings() {
        size(800, 800, P2D);
    }


    private int white = color(255);
    private int grayLight = color(200);

    private float value = 0;
    private float precision = 1f;
    private float sliderWidth, sliderHeight, leftEdgeX, rightEdgeX, topEdgeY, botEdgeY, leftEdgeValue, rightEdgeValue;

    public void setup() {
        surface.setAlwaysOnTop(true);
    }

    public void draw() {
        translate(width * .5f, height * .5f);
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
            value += valueSpaceDelta;
        }
        infiniteSlider("infinite", precision);
        fill(grayLight);
        text(nf(value, 0, 0), 0, topEdgeY-25);
        gui();
    }

    private void infiniteSlider(String name, float precision) {
        sliderWidth = slider("width", 800);
        sliderHeight = slider("height", 120);
        leftEdgeX = -sliderWidth * .5f;
        rightEdgeX = sliderWidth * .5f;
        topEdgeY = -sliderHeight * .5f;
        botEdgeY =  sliderHeight * .5f;
        leftEdgeValue = -precision;
        rightEdgeValue = precision;
        drawHorizontalLine();
        drawMarkerLines(precision * 1.0f, sliderHeight * .4f, true);
        drawMarkerLines(precision * 0.5f, sliderHeight * .2f, false);
        drawMarkerLines(precision * .05f, sliderHeight * .1f, false);
        drawSelectionBox();
    }

    private void drawHorizontalLine() {
        stroke(grayLight);
        strokeWeight(1);
        line(leftEdgeX, 0, rightEdgeX, 0);
    }

    private  void drawSelectionBox(){
        stroke(white);
        noFill();
        rectMode(CENTER);
        rect(0, 0, 10, sliderHeight*slider("selector height",0, 1,  .7f), 5);
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
                float displayValue = moduloValue+value;
                String displayText = nf(displayValue, 0, 0);
                if(displayText.equals("-0")){
                    displayText = "0"; // -0 is just silly
                }
                fill(grayLight);
                textAlign(CENTER, CENTER);
                textSize(slider("text size", 50));
                text(displayText, screenX, botEdgeY + 15);
            }
            markerValue += frequency;
        }
    }

}
