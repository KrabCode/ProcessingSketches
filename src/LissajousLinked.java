import applet.HotswapGuiSketch;
import processing.core.PVector;

import java.util.ArrayList;

public class LissajousLinked extends HotswapGuiSketch {
    float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("LissajousLinked");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        colorMode(HSB, 1, 1, 1, 1);
        background(0);
        recordingFrames = 360 * 2;
    }

    public void draw() {
        t += radians(.5f);
        bg();
        int res = floor(slider("res", 6));
        for (int ix = 0; ix < res; ix++) {
            for (int iy = 0; iy < res; iy++) {
                float xNorm = norm(ix, 0, res - 1);
                float yNorm = norm(iy, 0, res - 1);
                float r = slider("r", 300);
                float margin = r * 1.2f;
                float xPos = map(xNorm, 0, 1, margin, width - margin);
                float yPos = map(yNorm, 0, 1, margin, height - margin);
                float xFreq = slider("x frq start", 5);
                float yFreq = slider("y frq start", 5);
                xFreq += xNorm * slider("frq range", 5);
                yFreq += yNorm * slider("frq range");
                pushMatrix();
                translate(xPos, yPos);
                if (toggle("points")) {
                    stroke(1);
                    strokeWeight(3);
                    point(0, 0);
                }
                updatePs(r, xFreq, yFreq);
                popMatrix();
            }
        }
        rec();
        gui();
    }

    void updatePs(float r, float xFreq, float yFreq) {
        ArrayList<PVector> ps = new ArrayList<PVector>();
        float count = slider("count", 100);
        for (int i = 0; i < count; i++) {
            float in = norm(i, 0, count - 1);
            float ix = r * cos(in * TWO_PI * xFreq + t);
            float iy = r * sin(in * TWO_PI * yFreq + t);
            ps.add(new PVector(ix, iy, in));
        }
        beginShape(LINES);
        strokeWeight(1);
        for (PVector a : ps) {
            for (PVector b : ps) {
                if (a.equals(b)) {
                    continue;
                }
                float d = dist(a.x, a.y, b.x, b.y);
                float w = pow(1 / ((d / r) + 1), slider("distance", 12));
                float hueStart = slider("hue start");
                float hueRange = slider("hue range");
                stroke((hueStart + hueRange * a.z) % 1, 1, 1, w);
                vertex(a.x, a.y);
                stroke((hueStart + hueRange * b.z) % 1, 1, 1, w);
                vertex(b.x, b.y);
            }
        }
        endShape();
    }

    void bg() {
        blendMode(SUBTRACT);
        noStroke();
        fill(1, slider("alpha", 1));
        rect(0, 0, width, height);
        blendMode(BLEND);
    }
}
