import applet.GuiSketch;
import applet.HotswapGuiSketch;

public class ConcentricClouds extends HotswapGuiSketch {
    private float t;

    public static void main(String[] args) {
        GuiSketch.main("ConcentricClouds");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        colorMode(HSB, 1, 1, 1);
    }

    public void draw() {
        t += radians(slider("t"));
        String background = "concentric/background.glsl";
        uniform(background).set("time", t);
        hotFilter(background);
    }


}
