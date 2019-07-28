import applet.CustomPApplet;

public class Smoke extends CustomPApplet {
    public static void main(String[] args) {
        CustomPApplet.main("Smoke");
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
        cigarette();
        smoke();
    }

    private void cigarette() {

    }

    private void smoke(){

    }
}
