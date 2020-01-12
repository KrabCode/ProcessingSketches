import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class Feedback extends KrabApplet {
    private PGraphics pg;
    private PImage img;

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
        img = loadImage(randomImageUrl(800));
        frameRecordingDuration *= 4;
    }

    public void draw() {
        pg.beginDraw();
        if(button("new image")) {
            img = loadImage(randomImageUrl(800));
            pg.image(img, 0, 0);
        }
        if(toggle("draw image")){
            pg.tint(255,255,255,slider("alpha", 50));
            pg.image(img, 0, 0);
            pg.noTint();
        }
        feedbackMovePass(pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

}
