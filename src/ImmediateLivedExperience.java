import applet.KrabApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

public class ImmediateLivedExperience extends KrabApplet {
    private PGraphics pg;
    private PFont henrik;
    private float henrikSize = 40;
    PImage img;

    public static void main(String[] args) {
        ImmediateLivedExperience.main("ImmediateLivedExperience");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        img = loadImage("images/yellow_clouds_crop.jpg");
        henrik = createFont("fonts\\Henrik-Regular.otf", henrikSize);
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
