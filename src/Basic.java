import applet.Sketch;

public class Basic extends Sketch {
    public static void main(String[] args) {
        Sketch.main("Basic");
    }

    public void settings() {
        size(600, 600, P3D);
    }

    public void setup() {
        super.setup();
    }

    public void draw() {
        super.draw();
        background(0);
        gui();
    }
}
