import applet.GuiSketch;

public class Basic extends GuiSketch {
    public static void main(String[] args) {
        GuiSketch.main("Basic");
    }

    public void settings() {
        size(600, 600, P3D);
    }

    public void setup() {
        super.setup();
    }

    public void draw() {
        super.draw();
        background(slider("background", 255));
        gui();
    }
}
