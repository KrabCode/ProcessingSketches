import applet.GuiSketch;
import ch.bildspur.postfx.builder.PostFX;
import peasy.PeasyCam;

public class DotPlane extends GuiSketch {
    public static void main(String[] args) {
        GuiSketch.main("DotPlane");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    float t = 0;
    PeasyCam cam;
    PostFX fx;

    int recStarted = -1;
    int recDuration = 360;

    int count = 50;
    float[][] weights;

    @Override
    public void keyPressed() {
        recStarted = frameCount;
    }

    public void setup() {
        super.setup();
        cam = new PeasyCam(this, 200);
        fx = new PostFX(this);
    }

    public void draw() {
        super.draw();
        t += radians(1);
        background(0);
        updateWeights();
        drawDots();
        cam.beginHUD();
        if (toggle("chromatic")) {
            fx.render().chromaticAberration().compose();
        }
        cam.endHUD();
        if (recStarted > 0 && frameCount <= recStarted + recDuration) {
            saveFrame(captureDir + "####.jpg");
        }
        gui();
    }

    private void updateWeights() {
        int oldCount = count;
        count = floor(slider("count", 400));
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
                strokeWeight(weights[xIndex][zIndex]);
                point(x, y, z);
            }
        }
    }
}
