import applet.GuiSketch;
import peasy.PeasyCam;
import processing.core.PVector;
import utils.OpenSimplexNoise;

import java.util.ArrayList;

public class Lorenz extends GuiSketch {
    private OpenSimplexNoise noise = new OpenSimplexNoise();
    private ArrayList<Point> path = new ArrayList<Point>();
    private float x = .01f;
    private float y = 0.f;
    private float z = 0.f;
    private float a = 10;
    private float b = 28;
    private float c = 8 / 3f;
    private float hueTime = 0;
    private float noiseTime = 0;

    public static void main(String[] args) {
        GuiSketch.main("Lorenz");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        colorMode(HSB, 1, 1, 1, 1);
        new PeasyCam(this, 200);
    }

    public void draw() {
        hueTime += radians(slider("hue speed", 0, 1, .05f));
        noiseTime += radians(slider("noise speed"));

        int clr = color((hueTime * .05f) % 1, 1, 1);
        float dt = slider("dt", .01f);
        float dx = a * (y - x);
        float dy = x * (b - z) - y;
        float dz = x * y - c * z;
        x += dx * dt;
        y += dy * dt;
        z += dz * dt;
        path.add(new Point(new PVector(x, y, z), clr));
        while (path.size() > slider("count", 3000)) {
            path.remove(0);
        }

        background(0);
        beginShape();
        noFill();
        strokeWeight(slider("weight", 0, 3));
        float freq = slider("freq", .5f);
        float mag = slider("mag", 3);
        for (Point p : path) {
            stroke(p.color);
            PVector n = new PVector(
                    mag * (-1 + 2 * (float) noise.eval(20 + p.pos.x * freq, p.pos.y * freq, p.pos.z * freq, noiseTime)),
                    mag * (-1 + 2 * (float) noise.eval(50 + p.pos.x * freq, p.pos.y * freq, p.pos.z * freq, noiseTime)),
                    mag * (-1 + 2 * (float) noise.eval(80 + p.pos.x * freq, p.pos.y * freq, p.pos.z * freq, noiseTime))
            );
            vertex(p.pos.x + n.x, p.pos.y + n.y, p.pos.z + n.z);
        }
        endShape();

        gui();
    }

    class Point {
        PVector pos;
        int color;

        Point(PVector pos, int color) {
            this.pos = pos;
            this.color = color;
        }
    }
}
