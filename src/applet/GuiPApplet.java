package applet;

import javafx.scene.control.Slider;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;

public class GuiPApplet extends PApplet {
    private String name = this.getClass().getSimpleName();
    private String id = name + "_" + year() + nf(month(), 2) + nf(day(), 2) + "-" + nf(hour(), 2) + nf(minute(), 2) + nf(second(), 2);
    protected String captureDir = "out/capture/" + id + "/";

    public void setup() {
        if (width < 1000) {
            surface.setLocation(1920 - width - 20, 20);
        }
        hud = createGraphics(width / 4, height);
    }

    public void draw() {
        float nonFlickeringFrameRate = frameRate > 58 && frameRate < 62 ? 60 : frameRate;
        surface.setTitle(name + " (" + floor(nonFlickeringFrameRate) + " fps)");
    }

    PGraphics hud;

    protected boolean bang(String name){
        return false;
    }

    protected boolean toggle(String name, boolean initial) {
        return false;
    }

    protected float slider(String name, float min, float max) {
        float range = max - min;
        return slider(name, min, max, min + range / 2);
    }

    protected float slider(String name, float min, float max, float initial) {
        return initial;
    }

    private boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }
}
