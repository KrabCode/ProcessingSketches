import applet.GuiSketch;
import utils.OpenSimplexNoise;

public class GridOfDiagonals extends GuiSketch {

    float t = 0;
    float tRecStart = -1;
    float tRecFinish = -1;

    public static void main(String[] args) {
        GuiSketch.main("GridOfDiagonals");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        colorMode(HSB, 1, 1, 1, 1);

    }

    public void draw() {
        t += radians(1);
        background(slider("bg"));
        stroke(slider("fg"));
        strokeCap(ROUND);
        strokeWeight(slider("weight", 5));
        noFill();
        grid();
        record();
        gui();
    }

    private void record() {
        if (tRecStart > 0 && frameCount <= tRecFinish) {
            regenId();
            saveFrame(captureDir + "####.jpg");
        }
    }

    public void keyPressed() {
        tRecStart = frameCount;
        tRecFinish = frameCount + 360;
    }

    private void grid() {
        int side = floor(slider("side", 100));
        float xSize = width / side;
        float ySize = height / side;
        for (int x = 0; x <= width + xSize; x += xSize) {
            for (int y = 0; y <= height + ySize; y += ySize) {
                pushMatrix();
                translate(x, y);
                float d = dist(x, y, width * .5f, height * .5f);
                float a = atan2(height * .5f - y, width * .5f - x);
                float s = slider("s");
                if (sin(pow(d, slider("d")) - a * floor(slider("a", 20)) - t) > 0) {
                    line(-xSize * s, -ySize * s, xSize * s, ySize * s);
                } else {
                    line(-xSize * s, ySize * s, xSize * s, -ySize * s);
                }
                popMatrix();
            }
        }
        beginShape();
        endShape();
    }

}
