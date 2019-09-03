import applet.GuiSketch;
import applet.HotswapGuiSketch;
import processing.core.PImage;

public class ShaderStudio extends HotswapGuiSketch {

    private float t = 0;
    private int frameRecordingEnds = 0;
    private boolean keyWasPressed = false;

    String saturationVibrance = "postFX/saturationVibranceFrag.glsl";
    String rgbSplit = "postFX/rgbSplitFrag.glsl";
    String noise = "postFX/noiseFrag.glsl";
    String wave = "shaderStudio/wave.glsl";

    PImage img;

    public static void main(String[] args) {
        GuiSketch.main("ShaderStudio");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        img = loadImage("images/0.jpg");
    }

    public void draw() {
        image(img, 0, 0, width, height);
        t += radians(slider("t", 0,1,1));

        noisePass();
        saturationVibrancePass();
        rgbSplitPass();

        screenshot();
        rec();
        gui(false);
    }

    private void noisePass() {
        uniform(noise).set("time", t);
        uniform(noise).set("amount", slider("noise amt", 1));
        uniform(noise).set("speed",  slider("noise spd", 10));
        hotFilter(noise);
    }

    private void wave() {
        uniform(wave).set("time", t);
        hotFilter(wave);
    }

    private void rgbSplitPass() {
        uniform(rgbSplit).set("delta", slider("delta", 100));
        hotFilter(rgbSplit);
    }

    private void saturationVibrancePass() {
        uniform(saturationVibrance).set("saturation", slider("saturation", 5));
        uniform(saturationVibrance).set("vibrance", slider("vibrance", 2));
        hotFilter(saturationVibrance);
    }

    private void screenshot() {
        boolean keyJustReleased = keyWasPressed && !keyPressed;
        keyWasPressed = keyPressed;
        if(keyJustReleased && key == 's'){
            saveFrame(captureDir +"####.jpg");
        }
    }

    private void rec() {
        if (frameCount < frameRecordingEnds) {
            saveFrame(captureDir);
        }
    }

    public void keyPressed() {
        if (key == 'k') {
            frameRecordingEnds = frameCount + 300;
        }

    }
}
