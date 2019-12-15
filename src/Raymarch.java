import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2019-12-07
 */
public class Raymarch extends KrabApplet {
    private PGraphics pg;
    int scaledown = 8;

    public static void main(String[] args) {
        KrabApplet.main("Raymarch");
    }

    public void settings() {
        size(800,800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        resetPGraphics();
        frameRecordingDuration *= 2;
    }

    private void resetPGraphics() {
        pg = createGraphics(width/scaledown, height/scaledown, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        int intendedScaledown = sliderInt("scaledown", 1);
        if(intendedScaledown > 0 && intendedScaledown != scaledown){
            scaledown = intendedScaledown;
            resetPGraphics();
        }
        pg.beginDraw();
        rayMarchPass(pg);
        pg.endDraw();
        image(pg, 0, 0, width, height);
        rec(pg);
        gui();
    }
}
