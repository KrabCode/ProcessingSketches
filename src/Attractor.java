import applet.KrabApplet;
import processing.core.PVector;
import utils.OpenSimplexNoise;

import java.util.ArrayList;

public class Attractor extends KrabApplet {
    private OpenSimplexNoise noise = new OpenSimplexNoise();

    private int frameRecordingEnds = 0;
    ArrayList<Path> paths = new ArrayList<>();

    private float a = 10;
    private float b = 28;
    private float c = 8 / 3f;
    private float hueSpeed = 0;
    private float noiseTime = 0;
    private float rotY = 0;
    float range = 0;

    public static void main(String[] args) {
        KrabApplet.main("Attractor");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        colorMode(HSB, 1, 1, 1, 1);
    }

    public void draw() {
        hueSpeed = radians(slider("hue speed", 0, .01f, .005f));
        range = slider("colorRange",0,1,.1f);

        background(0);

        cam();

        int pathCount = floor(slider("pathCount", 30));
        if(button("regen")){
            paths.clear();
        }
        float r = slider("start r", 20);
        while (paths.size() < pathCount) {
            PVector gaussian3D = new PVector(r*randomGaussian(), r*randomGaussian(), r*randomGaussian());
            paths.add(new Path(gaussian3D));
        }
        while (paths.size() > pathCount) {
            paths.remove(paths.size()-1);
        }

        noiseTime += radians(slider("noise speed"));
        for (Path p : paths) {
            p.update();
        }

        rec();
        gui();
    }

    class Path {

        ArrayList<Point> path = new ArrayList<Point>();
        Path(PVector pos) {
            path.add(new Point(new PVector(pos.x, pos.y, pos.z), random(range)+noise(radians(frameCount))));
        }

        void update() {
            Point last = path.get(path.size()-1);
            float x = last.pos.x;
            float y = last.pos.y;
            float z = last.pos.z;
            float newHue = (last.hue + hueSpeed)%1;
            float dt = slider("speed", .03f);

            float dx = a * (y - x);
            float dy = x * (b - z) - y;
            float dz = x * y - c * z;
            x += dx * dt;
            y += dy * dt;
            z += dz * dt;

            path.add(new Point(new PVector(x, y, z), newHue));
            while (path.size() > slider("path length", 0, 300, 40)) {
                path.remove(0);
            }

            beginShape();
            noFill();
            strokeWeight(slider("weight", 1.9f, 3));

            float freq = slider("noise freq", .5f);
            float mag = slider("noise mag", 3);
            for (Point p : path) {
                float indexNorm = norm(path.indexOf(p), 0, path.size()-1);
                stroke(p.hue, 1-constrain(pow(indexNorm, 2.8f), 0, 1), 1, 1);
                PVector n = new PVector(
                        mag *  noise(20 + p.pos.x * freq + p.pos.y * freq +p.pos.z * freq, noiseTime),
                        mag *  noise(50 + p.pos.x * freq + p.pos.y * freq +p.pos.z * freq, noiseTime),
                        mag *  noise(80 + p.pos.x * freq + p.pos.y * freq +p.pos.z * freq, noiseTime)
                );
                vertex(p.pos.x + n.x, p.pos.y + n.y, p.pos.z + n.z);
            }
            endShape();
        }
    }
    class Point {
        PVector pos;
        float hue;

        Point(PVector pos, float hue) {
            this.pos = pos;
            this.hue = hue;
        }
    }
}


