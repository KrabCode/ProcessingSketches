import applet.HotswapGuiSketch;

public class Test extends HotswapGuiSketch {
    public static void main(String[] args) {
        HotswapGuiSketch.main("PACKAGE_NAME.Test");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {

    }

    public void draw() {

        rec();
        gui();
    }
}
