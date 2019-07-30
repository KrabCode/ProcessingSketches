import applet.GuiPApplet;
import applet.Sketch;

public class SliderTest extends GuiPApplet {
    public static void main(String[] args) {
        Sketch.main("SliderTest");
    }

    public void settings() {
        size(600, 600, P2D);
    }

    public void setup() {
        super.setup();
    }

    int bgColor = 0;

    public void draw() {
        super.draw();
        background(bgColor);
        bgColor = floor(slider("background", 0, 255, 0));
        translate(width * .5f, height * .5f);

        if (button("test 1")) {
            println("test 1");
        }
        if (button("test 2")) {
            println("test 2");
        }
        if (button("test 3")) {
            println("test 3");
        }
        rectMode(CENTER);
        for (int xi = 0; xi < 5; xi++) {
            for (int yi = 0; yi < 5; yi++) {
                stroke(slider("stroke", 0, 255, 100));
                strokeWeight(slider("weight", 0, 20));
                fill(slider("fill", 0, 255, 200));
                if (toggle("noFill", false)) {
                    noFill();
                }
                rect(0, 0, 50, 50);
            }
        }
        line(-width, 0, width, 0);
        line(0, -height, 0, height);
    }
}