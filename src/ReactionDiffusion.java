import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

// based on http://karlsims.com/rd.html

public class ReactionDiffusion extends KrabApplet {
    PImage img;
    private int blue = color(0, 0, 255);
    private int red = color(255, 0, 0);
    private int black = color(0);
    private int white = color(255);
    private PGraphics pg;
    private PGraphics bw;
    private PGraphics raymarchPG;
    private String algorithm = "";

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
        raymarchPG = createGraphics(width, height, P2D);
        frameRecordingDuration *= 2;
        img = loadImage(randomImageUrl(800));
    }


    public void draw() {
        pg.beginDraw();
        group("seed");
        if (button("new image")) {
            img = loadImage(randomImageUrl(800));
            pg.image(img, 0, 0, width, height);
        }

        if (toggle("keep seed")) {
            drawSeed(algorithm.equals("gray-scott")?blue:white);
        }
        if (button("new seed")) {
            pg.background(algorithm.equals("gray-scott")?red:black);
            drawSeed(algorithm.equals("gray-scott")?blue:white);
        }
        group("rd");
        algorithm = options("gray-scott", "blur-sharpen");
        int passes = sliderInt("passes", 1);
        for (int i = 0; i < passes; i++) {
            if (algorithm.equals("gray-scott")) {
                reactionDiffusionGrayScottPass();
            } else {
                reactionDiffusionBlurSharpenPass(i);
            }
        }
        if(toggle("feedback move")){
            feedbackMovePass(pg);
        }
        pg.endDraw();
        bw.beginDraw();
        bw.noTint();
        bw.image(pg, 0, 0, width, height);
        if (algorithm.equals("gray-scott")) {
            String redBlueToBlackWhite = "rbToColor.glsl";
            hotFilter(redBlueToBlackWhite, bw);
        }
        bw.endDraw();
        rec(bw);
        if(toggle("keep image") && img != null){
            bw.beginDraw();
            bw.tint(255,255,255,slider("image alpha"));
            bw.image(img, 0, 0, width, height);
            bw.endDraw();
        }

        image(bw, 0, 0, width, height);
        gui();
    }

    private void drawSeed(int clr) {
        pg.pushMatrix();
        pg.stroke(clr);
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

    private void reactionDiffusionGrayScottPass() {
        String rd = "reactionDiffusionGrayScott.glsl";
        uniform(rd).set("time", t);
        if(img != null){
            uniform(rd).set("parameterMap", img);
        }
        uniform(rd).set("diffA", slider("diffusion a", 0, 1, 1.f));
        uniform(rd).set("diffB", slider("diffusion b", 0, 1, .5f));
        uniform(rd).set("feed", slider("feed", 0, .1f, .055f));
        uniform(rd).set("kill", slider("kill", 0, .1f, .062f));
        hotFilter(rd, pg);
    }

    private void reactionDiffusionBlurSharpenPass(int passIndex) {
        String rd = "reactionDiffusionBlurSharpen.glsl";
        uniform(rd).set("passIndex", passIndex);
        hotFilter(rd, pg);
    }
}
