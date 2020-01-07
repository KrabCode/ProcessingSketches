import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

// based on http://karlsims.com/rd.html

public class ReactionDiffusion extends KrabApplet {
    PImage img;
    int blue = color(0, 0, 255);
    int red = color(255, 0, 0);
    private PGraphics pg;
    private PGraphics bw;

    public static void main(String[] args) {
        KrabApplet.main("ReactionDiffusion");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        surface.setLocation(1920 - 820, 20);
        pg = createGraphics(width, height, P2D);
        bw = createGraphics(width, height, P2D);
    }


    public void draw() {
        pg.beginDraw();
        group("seed");
        if (button("new seed") || frameCount < 5) {
            drawSeed();
        }
        if (button("new image")) {
            img = loadImage(randomImageUrl(800));
            pg.image(img, 0, 0, width, height);
        }
        group("rd");
        int passes = sliderInt("passes");
        for (int i = 0; i < passes; i++) {
            reactionDiffusionPass();
        }
        pg.endDraw();
        bw.beginDraw();
        bw.image(pg, 0, 0, width, height);
        String redBlueToBlackWhite = "rbbw.glsl";
        hotFilter(redBlueToBlackWhite, bw);
        bw.endDraw();
        image(bw, 0, 0, width, height);
        rec(bw);
        gui();
    }

    private void drawSeed() {
        pg.background(red);
        pg.strokeWeight(5);
        pg.stroke(blue);
        pg.noFill();
        pg.translate(width*.5f, height*.5f);
        int count = sliderInt("count", 1);
        float size = slider("sq size", 30);
        float r = slider("r", 200);
        for (int i = 0; i <= count; i++) {
            float a = map(i, 0, count, 0, TAU);
            pg.pushMatrix();
            pg.rotate(a);
            pg.translate(r, 0);
            pg.rect(0, 0, size, size);
            pg.rectMode(CENTER);
            pg.popMatrix();
        }
    }

    private void reactionDiffusionPass() {
        String rd = "reactionDiffusion.glsl";
        uniform(rd).set("dA", slider("diffusion a", 0, 1, 1.f));
        uniform(rd).set("dB", slider("diffusion b", 0, 1, .5f));
        uniform(rd).set("feed", slider("feed", 0, .1f, .055f));
        uniform(rd).set("kill", slider("kill", 0, .1f, .062f));
        hotFilter(rd, pg);
    }
}