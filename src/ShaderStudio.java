import applet.GuiSketch;
import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PImage;

public class ShaderStudio extends HotswapGuiSketch {

    private float t = 0;
    private int frameRecordingEnds = 0;
    private int recordingFrames = 60*30;
    private boolean keyWasPressed = false;

    private PImage img;
    private PGraphics pg;

    public static void main(String[] args) {
        GuiSketch.main("ShaderStudio");
    }

    public void settings() {
//        fullScreen(P2D, 1);
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        background(0);
        pg = createGraphics(width, height, P2D);
        img = loadImage("images/lukasz-lada-q7z-AUlHPaw-unsplash.jpg");  // Photo by Łukasz Łada on Unsplash
    }


    public void draw() {
        t += radians(1 / 2f);
        pg.beginDraw();
//        wave();
//        noisePass();
//        sobelPass();

        image();

        if (toggle("ndps", true)) {
            noiseOffsetPass();
        }

        rgbSplitPass();

        if (toggle("sat/vib", false)) {
            saturationVibrancePass();
        }

        pg.endDraw();
        image(pg, 0, 0, width, height);
        screenshot();
        rec();
        gui(false);
    }

    private void sobelPass() {
        String sobel = "postFX/sobelFrag.glsl";
        hotFilter(sobel, pg);
    }

    private void image() {
        if (button("reset image") || img == null) {
            regenImage();
        }
        pg.resetShader();
        float recordingMultiplier = 1;
        if (isRecording()) {
            recordingMultiplier = constrain(1 - recordingTimeNormalized() * 2, 0, 1);
        }
        pg.tint(255, constrain(255 * slider("opacity") * recordingMultiplier, 5, 255));
        pg.image(img, 0, 0, width, height);
    }

    private void regenImage() {
        img = loadImage(randomImageUrl(width, height));
    }

    private boolean isRecording() {
        return frameCount < frameRecordingEnds;
    }

    private float recordingTimeNormalized() {
        return norm(frameCount, frameRecordingEnds - recordingFrames, frameRecordingEnds);
    }

    private void noiseOffsetPass() {
        String noiseOffset = "shaderStudio/noiseOffset.glsl";
        uniform(noiseOffset).set("time", t);
        uniform(noiseOffset).set("mixAmt", slider("mix", 1));
        uniform(noiseOffset).set("mag", slider("mag", .005f));
        uniform(noiseOffset).set("frq", slider("frq", 0, 10, 2.5f));
        hotFilter(noiseOffset, pg);
    }

    private void noisePass() {
        String noise = "postFX/noiseFrag.glsl";
        uniform(noise).set("time", t);
        uniform(noise).set("amount", slider("noise amt", 1));
        uniform(noise).set("speed", slider("noise spd", 10));
        hotFilter(noise, pg);
    }

    private void wave() {
        String wave = "shaderStudio/wave.glsl";
        uniform(wave).set("time", t);
        hotFilter(wave, pg);
    }

    private void rgbSplitPass() {
        String rgbSplit = "postFX/rgbSplitFrag.glsl";
        uniform(rgbSplit).set("delta", slider("delta", 100));
        hotFilter(rgbSplit, pg);
    }

    private void saturationVibrancePass() {
        String saturationVibrance = "postFX/saturationVibranceFrag.glsl";
        uniform(saturationVibrance).set("saturation", slider("saturation", 0, 0.5f, 0));
        uniform(saturationVibrance).set("vibrance", slider("vibrance", 0, 0.5f, 0));
        hotFilter(saturationVibrance, pg);
    }

    private void screenshot() {
        boolean keyJustReleased = keyWasPressed && !keyPressed;
        keyWasPressed = keyPressed;
        if (keyJustReleased && key == 's') {
            pg.save(captureDir + frameCount + ".jpg");
        }
    }

    private void rec() {
        if (frameCount < frameRecordingEnds) {
            println(frameCount - frameRecordingEnds + recordingFrames + " / " + recordingFrames);
            pg.save(captureDir + frameCount + ".jpg");
        }
    }

    public void keyPressed() {
        if (key == 'k') {
            frameRecordingEnds = frameCount + recordingFrames + 1;
        }
        if (key == 'i') {
            img.save(captureDir + frameCount + ".jpg");
        }
        if (key == 'r') {
            frameRecordingEnds = frameCount - 1;
        }
    }
}
