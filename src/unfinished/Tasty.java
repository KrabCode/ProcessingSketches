package unfinished;

import applet.HotswapGuiSketch;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;

public class Tasty extends HotswapGuiSketch {
    private float t;
    PFont myFont;
    private PGraphics backgroundGraphics;
    private PVector bubbleFrameSize;
    private ArrayList<PImage> bubbleFrames = new ArrayList<PImage>();
    private ArrayList<PGraphics> bubbleGraphics = new ArrayList<PGraphics>();
    private ArrayList<Emitter> emitters = new ArrayList<>();
    private float emitterRadiusVariation, bubbleVariation, emitterSpawnFreqVariation;

    public static void main(String[] args) {
        HotswapGuiSketch.main("unfinished.Tasty");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        bubbleFrames = loadImages("data\\animation\\tvars_10fps\\kruh2fill");
        bubbleFrameSize = new PVector(bubbleFrames.get(0).width, bubbleFrames.get(0).height);
        surface.setAlwaysOnTop(true);
        backgroundGraphics = createGraphics(width, height, P2D);
        for (int i = 0; i < bubbleFrames.size(); i++) {
            bubbleGraphics.add(createGraphics(bubbleFrames.get(0).width, bubbleFrames.get(0).height, P2D));
        }
        myFont = createFont("data/fonts/Moonbright Demo.ttf", 100);
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        backgroundGraphics.beginDraw();
        emitterRadiusVariation = slider("emitter variation", 0, .1f);
        bubbleVariation = slider("bubble variation", 0, .1f);
        emitterSpawnFreqVariation = floor(slider("spawn var", 30));
        uniform("bubbleBackground.glsl").set("time", t);
        hotFilter("bubbleBackground.glsl", backgroundGraphics);
        backgroundGraphics.fill(slider("r", 255),slider("g", 255),slider("b", 255));
        backgroundGraphics.textFont(myFont);
        backgroundGraphics.textSize(slider("text size", 200));
        backgroundGraphics.textAlign(CENTER, CENTER);
        backgroundGraphics.text("TASTY", width*.5f, height*.5f);
        updateEmitters();
        updateForegrounds();
        noiseOffsetPass(backgroundGraphics, t);
        backgroundGraphics.endDraw();
        image(backgroundGraphics, 0, 0);
        rec(backgroundGraphics);
        gui();
    }

    private void updateEmitters() {
        int intendedEmitterCount = floor(slider("emitters", 40));
        while (emitters.size() < intendedEmitterCount) {
            emitters.add(new Emitter());
        }
        while (emitters.size() > intendedEmitterCount) {
            emitters.remove(0);
        }
        for (Emitter e : emitters) {
            e.updateBubbles();
        }
    }

    private void updateForegrounds() {
        for (int i = 0; i < bubbleFrames.size(); i++) {
            PGraphics fg = bubbleGraphics.get(i);
            fg.beginDraw();
            fg.clear();
            fg.imageMode(CORNER);
            fg.image(bubbleFrames.get(i), 0, 0);
            uniform("bubble.glsl").set("time", t);
            hotFilter("bubble.glsl", fg);
            fg.endDraw();
        }
    }

    class Emitter {
        private float r = 1 + randomGaussian() * emitterRadiusVariation;
        private PVector pos = new PVector(random(-width*.5f, width+width*.5f), height*.5f + random(height));
        private ArrayList<Bubble> bubbles = new ArrayList<>();
        private ArrayList<Bubble> bubblesToRemove = new ArrayList<>();
        int emitterSpawnFreqOffset = floor(random(emitterSpawnFreqVariation));

        private void updateBubbles() {
            int intendedBubbleCount = floor(slider("bubbles", 100));
            int emitterSpawnFreq = floor(slider("spawn freq", 30));
            if (frameCount % (emitterSpawnFreq+emitterSpawnFreqOffset) == 0 && bubbles.size() < intendedBubbleCount) {
                bubbles.add(new Bubble(this));
            }
            while (bubbles.size() > intendedBubbleCount) {
                bubbles.remove(0);
            }
            for (Bubble bubble : bubbles) {
                bubble.update();
            }
            bubbles.removeAll(bubblesToRemove);
            bubblesToRemove.clear();
        }
    }

    class Bubble {
        Emitter parent;
        int currentFrameIndex = floor(random(bubbleFrames.size()));
        int frameOffset = floor(random(20));
        int frameStarted = frameCount;
        int fadeInDuration = 30;
        float r;
        PVector pos, spd = new PVector();

        Bubble(Emitter parent) {
            this.parent = parent;
            this.pos = new PVector(parent.pos.x, parent.pos.y).add(PVector.random2D());
            this.r = parent.r + randomGaussian()*bubbleVariation;
        }

        void update() {
            PVector acc = new PVector(slider("noise mag")*(1-2*noise(pos.x, pos.y)), -slider("gravity", 0, .22f));

            spd.add(acc);
            spd.mult(slider("drag", 0, 1, .9f));
            pos.add(spd);
            if (pos.y < -r * bubbleFrameSize.y * .5f) {
                parent.bubblesToRemove.add(this);
            }
            updateAnimation();
            drawBubble();
        }

        private void updateAnimation() {
            int animationSpeed = floor(slider("anim speed", 1, 17));
            if ((frameCount + frameOffset) % animationSpeed == 0) {
                currentFrameIndex++;
            }
            if (currentFrameIndex > bubbleFrames.size() - 1) {
                currentFrameIndex = 0;
            }
        }

        private void drawBubble() {
            float fadeInNorm = constrain(norm(frameCount, frameStarted, frameStarted+fadeInDuration), 0, 1);
            backgroundGraphics.push();
            backgroundGraphics.translate(pos.x, pos.y);
            backgroundGraphics.scale(abs(lerp(0, r + slider("scale", -1, 0), fadeInNorm)));
            backgroundGraphics.imageMode(CENTER);
            backgroundGraphics.image(bubbleGraphics.get(currentFrameIndex), 0, 0);
            backgroundGraphics.pop();
        }
    }

}
