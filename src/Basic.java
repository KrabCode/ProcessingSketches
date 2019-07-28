import applet.CustomPApplet;

public class Basic extends CustomPApplet {
    public static void main(String[] args) {
        CustomPApplet.main("Basic");
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
