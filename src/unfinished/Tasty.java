package unfinished;

import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;

public class Tasty extends HotswapGuiSketch {
    private ArrayList<Bubble> bubbles = new ArrayList<>();
    private float t;
    private PGraphics pg;
    private ArrayList<PImage> bubbleFrames = new ArrayList<PImage>();

    public static void main(String[] args) {
        HotswapGuiSketch.main("unfinished.Tasty");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        bubbleFrames = loadImages("data\\animation\\tvars_10fps\\kruh2fill");
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        Bubble prototype = new Bubble();
        prototype.pos = new PVector(width * .5f, height * .5f);
        bubbles.add(prototype);
    }

    public void draw() {
        t += radians(slider("t", 1, true));

        pg.beginDraw();
        pg.background(0);
        for (Bubble bubble : bubbles) {
            bubble.update();
        }
        pg.endDraw();

        image(pg, 0, 0);
        rec(pg);
        gui();
    }


    class Bubble {
        PVector pos = new PVector(), spd = new PVector(), acc = new PVector();
        int currentFrameIndex = floor(random(bubbleFrames.size()));
        PGraphics mg = createGraphics(bubbleFrames.get(0).width, bubbleFrames.get(0).height, P2D);

        void update() {
            updateAnimation();
            mg.beginDraw();
            mg.clear();
            mg.imageMode(CORNER);
            mg.image(bubbleFrames.get(currentFrameIndex), 0, 0);
            uniform("bubble.glsl").set("time", t);
            hotFilter("bubble.glsl", mg);
            mg.endDraw();
            pg.push();
            pg.translate(pos.x, pos.y);
            pg.scale(slider("scale", 10));
            pg.imageMode(CENTER);
            pg.image(mg, 0, 0);
            pg.pop();
        }

        private void updateAnimation() {
            int animationSpeed = floor(slider("anim speed", 1,30));
            recordingFrames = bubbleFrames.size() * animationSpeed;
            if (frameCount % animationSpeed == 0) {
                currentFrameIndex++;
                if (currentFrameIndex > bubbleFrames.size() - 1) {
                    currentFrameIndex = 0;
                }
            }
        }
    }


}
