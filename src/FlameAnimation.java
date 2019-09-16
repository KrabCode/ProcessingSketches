import applet.GuiSketch;
import applet.HotswapGuiSketch;
import com.studiohartman.jamepad.ControllerManager;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.io.File;
import java.util.ArrayList;

public class FlameAnimation extends HotswapGuiSketch {
    int currentAnimationFrame = 0;
    private PGraphics bg;
    private PGraphics fg;
    private ControllerManager controllers = new ControllerManager();
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
        controllers.initSDLGamepad();
        animationPos = new PVector(width * .5f, height * .5f);
        loadAnimationImages();
    }

    private void loadAnimationImages() {
        File animationFolder = new File("C:\\Projects\\ProcessingSketches\\data\\flameAnimation\\frames");
        for (final File file : animationFolder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            PImage frame = loadImage("flameAnimation\\frames\\" + file.getName());
            animation.add(frame);
        }
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
        if (5 * abs(controllers.getState(0).leftStickX) > 1 ||
            5 * abs(controllers.getState(0).leftStickY) > 1) {
            animationPos.x += 5 * controllers.getState(0).leftStickX;
            animationPos.y -= 5 * controllers.getState(0).leftStickY;
        }
        if (controllers.getState(0).aJustPressed) {
            animationPos = new PVector();
        }
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

    private void rec() {
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
