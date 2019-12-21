import applet.KrabApplet;
import processing.core.PGraphics;
import utils.OpenSimplexNoise;

public class NoiseTexture extends KrabApplet {
    PGraphics pg;
    private float freq = 0;
    private OpenSimplexNoise noise = new OpenSimplexNoise();

    public static void main(String[] args) {
        KrabApplet.main("NoiseTexture");
    }

    public void settings() {
        size(1024, 1024, P2D);
    }

    public void setup() {
        pg = createGraphics(width, height, P2D);
    }

    public void draw() {
        float intendedFreq = 50;
        if (freq != intendedFreq) {
            freq = intendedFreq;
            pg.beginDraw();
            pg.loadPixels();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    float xa = map(x, 0, width, 0, TAU);
                    float ya = map(y, 0, height, 0, TAU);
                    float n = (float) noise.eval(sin(xa) * freq,cos(xa) * freq, sin(ya) * freq, cos(ya) * freq);
                    pg.pixels[x + y * width] = color((.5f+.5f*n)*255);
                }
            }
            pg.updatePixels();
            pg.endDraw();
        }
        image(pg, 0, 0, width, height);
        rec();
        gui();
    }
}
