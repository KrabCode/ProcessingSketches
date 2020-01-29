import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Created by Jakub 'Krab' Rak on 2020-01-29
 */
public class TripReplication extends KrabApplet {
    private PGraphics pg;
    private PImage girlPearl;

    public static void main(String[] args) {
        KrabApplet.main("TripReplication");
    }

    public void settings() {
        int scaledown = 2;
        size(1808/scaledown,2136/scaledown, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        frameRecordingDuration *= 2;
        girlPearl = loadImage("images\\Girl_with_a_Pearl_Earring.jpg");
    }

    public void draw() {
        pg.beginDraw();
        String replication = "replication.glsl";
        uniform(replication).set("img", girlPearl);
        uniform(replication).set("time", t);
        hotFilter(replication, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
