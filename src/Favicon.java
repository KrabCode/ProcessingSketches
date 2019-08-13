import applet.GuiSketch;

public class Favicon extends GuiSketch {
    public static void main(String[] args) {
        GuiSketch.main("Favicon");
    }

    public void settings() {
        size(320,320, P2D);
    }

    public void setup() {
        super.setup();
    }

    public void draw() {
        super.draw();
        background(0);
        gui();
    }

    public void keyPressed(){
        saveFrame(captureDir+"####.jpg");
    }
}
