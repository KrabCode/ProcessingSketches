import applet.GuiSketch;
import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;

public class FlameAnimation extends HotswapGuiSketch {
    int currentAnimationFrame = 0;
    private PGraphics bg;
    private PGraphics fg;
    private float t;
    private int captureEndFrame;
    private int recordingFrames = 60 * 15;
    private float leftStickX;
    private float leftStickY;
    private ArrayList<PImage> animation = new ArrayList<PImage>();
    private PVector animationPos;

    public static void main(String[] args) {
        GuiSketch.main("FlameAnimation");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        bg = createGraphics(800, 800, P2D);
        fg = createGraphics(800, 800, P2D);
        animationPos = new PVector(width * .5f, height * .5f);
        animation = loadImages("C:\\Projects\\ProcessingSketches\\data\\flameAnimation\\frames");
    }

    public void draw() {
        t += radians(slider("time", 0, 1, 1));
        String flame = "flameAnimation\\flame.glsl";
        uniform(flame).set("time", t);
        bg.beginDraw();
        hotFilter(flame, bg);
        drawAnimation();
        bg.endDraw();
        image(bg, 0, 0, width, height);
        rec();
        gui(false);
    }

    private void drawAnimation() {
        if (frameCount % floor(slider("animate speed", 1, 20)) == 0) {
            currentAnimationFrame++;
        }
        currentAnimationFrame %= animation.size() - 1;
        fg.beginDraw();
        fg.background(255);
        fg.translate(animationPos.x, animationPos.y);
        fg.scale(slider("scale", 1));
        fg.imageMode(CENTER);
        fg.tint(0);
        fg.image(animation.get(currentAnimationFrame), 0,0);
        hotFilter("flameAnimation\\blackToWhite.glsl",fg);
        bg.image(fg,0,0);
        fg.endDraw();
    }

    public void rec() {
        if (isRecording()) {
            println(frameCount - captureEndFrame + recordingFrames + " / " + recordingFrames);
            bg.save(captureDir + frameCount + ".jpg");
        }
    }

    private boolean isRecording() {
        return frameCount < captureEndFrame;
    }


    public void keyPressed() {
        if (key == 'k') {
            id = regenId();
            captureEndFrame = frameCount + recordingFrames;
        }
    }
}
