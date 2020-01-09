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
        frameRecordingDuration *= 2;
    }


    public void draw() {
        pg.beginDraw();
        group("seed");
        if (button("new image")) {
            img = loadImage(randomImageUrl(800));
            pg.image(img, 0, 0, width, height);
        }
        if (toggle("keep seed")) {
            drawSeed();
        }
        if (button("new seed") || frameCount < 5) {
            pg.background(red);
            drawSeed();
        }
        group("rd");
        int passes = sliderInt("passes", 1);
        for (int i = 0; i < passes; i++) {
            reactionDiffusionPass();
        }
        pg.endDraw();
        bw.beginDraw();
        bw.image(pg, 0, 0, width, height);
        String redBlueToBlackWhite = "rbToColor.glsl";
        hotFilter(redBlueToBlackWhite, bw);
        bw.endDraw();
        image(bw, 0, 0, width, height);
        rec(bw);
        gui();
    }

    private void drawSeed() {
        pg.pushMatrix();
        pg.stroke(blue);
        pg.strokeWeight(slider("weight"));
        String type = options("grid", "circle");
        if (type.equals("grid")) {
            float step = slider("step", 5);
            for (int x = 0; x < width; x += step) {
                for (int y = 0; y < height; y += step) {
                    pg.point(x, y);
                }
            }
        } else if (type.equals("circle")) {
            pg.translate(width * .5f, height * .5f);
            int count = sliderInt("count");
            float r = slider("radius");
            for (int i = 0; i <= count; i++) {
                float angle = map(i, 0, count, 0, TAU);
                pg.point(r * cos(angle), r * sin(angle));
            }
        }
        pg.popMatrix();
    }

    private void reactionDiffusionPass() {
        String rd = "reactionDiffusion.glsl";
        uniform(rd).set("time", t);
        uniform(rd).set("dA", slider("diffusion a", 0, 1, 1.f));
        uniform(rd).set("dB", slider("diffusion b", 0, 1, .5f));
        uniform(rd).set("feed", slider("feed", 0, .1f, .055f));
        uniform(rd).set("kill", slider("kill", 0, .1f, .062f));
        hotFilter(rd, pg);
    }
}
