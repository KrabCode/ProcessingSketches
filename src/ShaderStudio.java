import applet.GuiSketch;
import applet.HotswapGuiSketch;
import processing.core.PImage;

public class ShaderStudio extends HotswapGuiSketch {

    private float t = 0;
    private int frameRecordingEnds = 0;
    String shaderPath = "postFX/rgbSplitFrag.glsl";
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
//        t += radians(slider("t", 0,1,1));
//        uniform(shaderPath).set("time", t);
        uniform(shaderPath).set("delta", slider("delta", 100));
        hotFilter(shaderPath);

        gui(false);
    }

    public void rec() {
        if (frameCount < frameRecordingEnds) {
            saveFrame(captureDir + "####.jpg");
        }
    }

    public void keyPressed() {
        frameRecordingEnds = frameCount + 600;
    }
}
