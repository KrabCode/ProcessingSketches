import applet.GuiSketch;
import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class DotPlane extends KrabApplet {
    public static void main(String[] args) {
        KrabApplet.main("DotPlane");
    }

    public void settings() {
        fullScreen(P3D);
    }

    float t = 0;

    int recStarted = -1;
    int recDuration = 360;

    int count = 10;
    float[][] weights;

    @Override
    public void keyPressed() {
        recStarted = frameCount;
    }

    public void setup() {
    }

    public void draw() {
        t += radians(1);
        background(0);
        updateWeights();
        cam();
        drawDots();
        gui();
        rec();
    }

    private void updateWeights() {
        int oldCount = count;
        count = floor(slider("count", 200));
        if (count != oldCount || weights == null) {
            weights = new float[count][count];
            for (int x = 0; x < count; x++) {
                for (int y = 0; y < count; y++) {
                    weights[x][y] = 1 + (randomGaussian() > 1.5f ? 1 : 0);
                }
            }
        }
    }

    void drawDots() {
        stroke(255);
        float size = slider("size", 600);
        int center = count / 2;
        for (int xIndex = 0; xIndex < count; xIndex++) {
            for (int zIndex = 0; zIndex < count; zIndex++) {
                float x = map(xIndex, 0, count - 1, -size * .5f, size * .5f);
                float z = map(zIndex, 0, count - 1, -size * .5f, size * .5f);
                float d = slider("freq", .5f) * dist(xIndex, zIndex, center, center);
                float a = floor(slider("freq 2", 24)) * atan2(z, x);
                float outwardWave = sin(d - t);
                float y = slider("y", 5) * outwardWave + (1 - outwardWave) * slider("y 2", 5) * sin(a);
                strokeWeight(slider("weight"));
                point(x, y, z);
            }
        }
    }
}
