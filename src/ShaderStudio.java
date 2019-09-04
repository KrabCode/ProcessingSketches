import applet.GuiSketch;
import applet.HotswapGuiSketch;
import processing.core.PImage;

public class ShaderStudio extends HotswapGuiSketch {

    private float t = 0;
    private int frameRecordingEnds = 0;
    private int recordingFrames = 360;
    private boolean keyWasPressed = false;

    private String saturationVibrance = "postFX/saturationVibranceFrag.glsl";
    private String rgbSplit = "postFX/rgbSplitFrag.glsl";
    private String noise = "postFX/noiseFrag.glsl";
    private String wave = "shaderStudio/wave.glsl";
    private String noiseDirectedPixelSort = "shaderStudio/noiseDirectedPixelSort.glsl";
    private PImage img;

    public static void main(String[] args) {
        GuiSketch.main("ShaderStudio");
    }

    public void settings() {
//        fullScreen(P2D, 2);
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        background(0);

    }


    private void regenImage() {
        img = loadImage(randomImageUrl(width,height));
    }

    public void draw() {
        if(toggle("image", true)){
            if(button("reset image") || img == null){
                regenImage();
            }
            if(isRecording()){
                tint(255, constrain(255-recordingTimeNormalized()*frameRecordingEnds, 0, 255));
            }else{
                tint(255,slider("opacity", 255));
            }
            image(img, 0, 0, width, height);
        }
        t += radians(slider("t", 0, 1, 1));

//        wave();
//        noisePass();
//        rgbSplitPass();
//        saturationVibrancePass();
        noiseDirectedPixelSort();

        screenshot();
        rec();
        gui(false);
    }

    private boolean isRecording() {
        return frameCount < frameRecordingEnds;
    }

    private float recordingTimeNormalized(){
        return norm(frameCount, frameRecordingEnds-recordingFrames,frameRecordingEnds);
    }

    private void noiseDirectedPixelSort() {
        uniform(noiseDirectedPixelSort).set("time", t);
        uniform(noiseDirectedPixelSort).set("timeRadius", slider("time radius", 1));
        uniform(noiseDirectedPixelSort).set("mag", slider("mag", .005f));
        uniform(noiseDirectedPixelSort).set("frq", slider("frq", 10));
        hotFilter(noiseDirectedPixelSort);
    }

    private void noisePass() {
        uniform(noise).set("time", t);
        uniform(noise).set("amount", slider("noise amt", 1));
        uniform(noise).set("speed", slider("noise spd", 10));
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
        if (keyJustReleased && key == 's') {
            saveFrame(captureDir + "####.jpg");
        }
    }

    private void rec() {
        if (frameCount < frameRecordingEnds) {
            saveFrame(captureDir);
        }
    }

    public void keyPressed() {
        if (key == 'k') {
            frameRecordingEnds = frameCount + recordingFrames;
        }
    }
}
