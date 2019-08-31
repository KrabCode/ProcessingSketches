import applet.GuiSketch;
import applet.HotswapGuiSketch;

public class Glyph extends HotswapGuiSketch {

    private float t = 0;
    private int frameRecordingEnds = 0;

    public static void main(String[] args) {
        GuiSketch.main("Glyph");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
    }

    public void draw() {
        background(0);
        t += radians(slider("t", 0,1,1));
        String shaderPath = "glyph.glsl";
        uniform(shaderPath).set("time", t);
        hotFilter(shaderPath);
        if (frameCount < frameRecordingEnds) {
            saveFrame(captureDir + "####.jpg");
        }
        gui(false);
    }

    public void keyPressed() {
        frameRecordingEnds = frameCount + 361;
    }
}
