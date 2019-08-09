package applet;

import processing.core.PApplet;

public abstract class BasicSketch extends PApplet {
    String sketchName = this.getClass().getSimpleName();
    private String id = sketchName + "_" + year() + nf(month(), 2) + nf(day(), 2) + "-" + nf(hour(), 2) + nf(minute(), 2) + nf(second(), 2);
    protected String captureDir = "out/capture/" + id + "/";

    public void setup() {
        if (width < 1000) {
            surface.setLocation(1920 - width - 20, 20);
        }
    }

    public void draw() {
        float nonFlickeringFrameRate = frameRate > 58 && frameRate < 62 ? 60 : frameRate;
        surface.setTitle(sketchName + " (" + floor(nonFlickeringFrameRate) + " fps)");
    }

}
