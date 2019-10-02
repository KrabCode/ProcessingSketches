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
        int res = floor(slider("res", 0, 6, 1));
        float cx = width * .5f;
        float cy = height * .5f;
        for (int ix = 0; ix < res; ix++) {
            for (int iy = 0; iy < res; iy++) {
                float xNorm = norm(ix, 0, res - 1);
                float yNorm = norm(iy, 0, res - 1);
                if (res == 1) {
                    xNorm = .5f;
                    yNorm = .5f;
                }
                float r = slider("r", 300);
                float xPos = map(xNorm, 0, 1, cx - width * .5f, cx + width * .5f);
                float yPos = map(yNorm, 0, 1, cy - height * .5f, cx + width * .5f);
                float xFreq = slider("x frq start", 5);
                float yFreq = slider("y frq start", 5);
                xFreq += xNorm * slider("frq range", 0,5, 0);
                yFreq += yNorm * slider("frq range");
                pushMatrix();
                translate(xPos, yPos);
                if (toggle("points")) {
                    stroke(1);
                    strokeWeight(3);
                    point(0, 0);
                }
                updateLissajous(r, xFreq, yFreq);
                popMatrix();
            }
        }
        rec();
        gui();
    }

    void updateLissajous(float r, float xFreq, float yFreq) {
        ArrayList<PVector> points = new ArrayList<PVector>();
        float count = slider("count", 300);
        for (int i = 0; i < count; i++) {
            float inorm = norm(i, 0, count - 1);
            float x = r * cos(inorm * TWO_PI * xFreq + t);
            float y = r * sin(inorm * TWO_PI * yFreq + t);
            if (toggle("modulated")) {
                float d = dist(x,y,0,0);
                float modFrq = slider("modFrq", 2.1f);
                float modMag = slider("modMag", 1f);
                points.add(new PVector(
                        x * (1.f+modMag*sin(d*modFrq)),
                        y * (1.f+modMag*sin(d*modFrq)),
                        inorm));
            } else {
                points.add(new PVector(x, y, inorm));
            }
        }
        beginShape(LINES);
        strokeWeight(1);
        for (PVector a : points) {
            for (PVector b : points) {
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
