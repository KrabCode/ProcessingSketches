import applet.Sketch;

public class SliderTest extends Sketch {

    public static void main(String[] args) {
        Sketch.main("SliderTest");
    }

    public void settings() {
        size(600, 600, P2D);
    }

    public void setup() {

    }

    float t = 0;

    public void draw() {
        colorMode(HSB, 1, 1, 1, 1);
        if(toggle("black", true)){
            background(0);
            stroke(1);
        }else{
            background(1);
            stroke(0);
        }
        if(button("a")){
            println("a");
        }
        if(button("b")){
            println("b");
        }
        t += slider("time", -10, 10);
        strokeWeight(1);
        noFill();
        if(toggle("fbm", true)){
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
        float freq = slider("freq", .1f);
        for (int i = 0; i < floor(slider("octaves", 8)); i++) {
            sum += amp * (1-2*noise(freq * x));
            amp *= slider("ampMod",1);
            freq *= slider("freqMod", 4);
        }
        return sum;
    }


}