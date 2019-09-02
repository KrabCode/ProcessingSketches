import applet.GuiSketch;
import applet.HotswapGuiSketch;
import processing.core.PImage;

public class ShaderStudio extends HotswapGuiSketch {

    private float t = 0;
    private int frameRecordingEnds = 0;
    PImage tex;

    public static void main(String[] args) {
        GuiSketch.main("ShaderStudio");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        tex = loadImage(picsum(800));
    }

    public void draw() {
        background(0);
        t += radians(slider("t", 0,1,1));
        String shaderPath = "shaderStudio/wave.glsl";
        uniform(shaderPath).set("time", t);
        hotFilter(shaderPath);
        if (frameCount < frameRecordingEnds) {
            saveFrame(captureDir + "####.jpg");
        }
        gui(false);
    }

    public void keyPressed() {
        frameRecordingEnds = frameCount + 600;
    }
}
