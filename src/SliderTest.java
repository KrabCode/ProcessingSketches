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
        surface.setResizable(true);
    }

    float t = 0;

    public void draw() {
        super.draw();
        colorMode(HSB, 1, 1, 1, 1);
        if(toggle("black", true)){
            background(0);
            stroke(1);
        }else{
            background(1);
            stroke(0);
        }
        if(button("test")){
            println("test");
        }
        t += slider("time", -10, 10);
        strokeWeight(1);
        noFill();
        if(toggle("fbm")){
            beginShape();
            for (int x = 0; x < width; x++) {
                float y = height * .5f + fbm(x - t);
                vertex(x, y);
            }
            endShape();
        }
        gui();
    }

    float fbm(float x) {
        float sum = 0;
        float amp = slider("amp", 0, 100, 5);
        float freq = slider("freq");
        for (int i = 0; i < floor(slider("octaves", 8)); i++) {
            sum += amp * sin(freq * x);
            amp *= slider("ampMod",1);
            freq *= slider("freqMod", 4);
        }
        return sum;
    }


}