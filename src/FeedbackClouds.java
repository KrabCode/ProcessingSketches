import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class FeedbackClouds extends KrabApplet {
    private PGraphics pg;
    PImage img;

    public static void main(String[] args) {
        FeedbackClouds.main("FeedbackClouds");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        img = loadImage("images/yellow_clouds_crop.jpg");
        pg = createGraphics(width, height, P2D);
        frameRecordingDuration *= 4;
    }

    public void draw() {
        group("core");
        if(button("new image")){
            img = loadImage(randomImageUrl(800, 800));
        }
        pg.beginDraw();
        pg.tint(255,255,255,slider("image alpha", 50));
        pg.image(img, 0, 0, width, height);
        pg.noTint();
        group("feedback");
        feedbackMovePass(pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
