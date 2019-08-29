import applet.GuiSketch;
import applet.HotswapGuiSketch;

public class ShaderStudio extends HotswapGuiSketch {

    public static void main(String[] args) {
        GuiSketch.main("ShaderStudio");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {

    }

    float t = 0;

    public void draw() {
        resetShader();
        background(0);
        t += radians(slider("t"));
        String shaderPath = "frag.glsl";
        uniform(shaderPath).set("time", t);
        hotFilter(shaderPath);
        gui();
    }

}
