import applet.KrabApplet;
import processing.core.PGraphics;

public class Feedback extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Feedback");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        surface.setLocation(1920-820,20);
        pg = createGraphics(width, height, P2D);
        frameRecordingDuration *= 4;
    }

    public void draw() {
        pg.beginDraw();
        seedPass(pg);
        feedbackMovePass(pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void seedPass(PGraphics pg) {
        String feedbackSeed = "feedbackSeed.glsl";
        uniform(feedbackSeed).set("time", t);
        uniform(feedbackSeed).set("alpha", slider("alpha"));
        hotFilter(feedbackSeed, pg);
    }

}
