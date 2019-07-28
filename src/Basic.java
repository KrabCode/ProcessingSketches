import processing.core.PApplet;

public class Basic extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Basic");
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
    }
}
