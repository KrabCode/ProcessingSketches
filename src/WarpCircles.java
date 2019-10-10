import applet.GuiSketch;
import peasy.PeasyCam;
import utils.OpenSimplexNoise;

public class WarpCircles extends GuiSketch {
    private float t;
    private OpenSimplexNoise noise = new OpenSimplexNoise();

    public static void main(String[] args) {
        GuiSketch.main("WarpCircles");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        PeasyCam cam = new PeasyCam(this, 600);
    }

    public void draw() {
        t += radians(slider("t"));
        background(0);
        stroke(255);
        strokeWeight(slider("weight", 1, 10, 1));
        noFill();
        int vertexCount = floor(slider("vertices", 60));
        int ringCount = floor(slider("rings", 30));
        float r = slider("r", 450);
        for (int ringIndex = 0; ringIndex < ringCount; ringIndex++) {
            float ringIndexNormalized = map(ringIndex, 0, ringCount - 1, 0, 1);

            beginShape();
            for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
                float vertexIndexNormalized = map(vertexIndex, 0, vertexCount - 1, 0, 1);
                float angle = vertexIndexNormalized * TWO_PI;
                float n0 = (float) noise.eval(
                        10 + slider("freq") * cos(angle),
                        10 + slider("freq") * sin(angle),
                        sin(t+ringIndexNormalized),
                        cos(t+ringIndexNormalized)
                );
                float n1 = (float) noise.eval(
                        30 + slider("freq") * cos(angle),
                        30 + slider("freq") * sin(angle),
                        sin(t+ringIndexNormalized),
                        cos(t+ringIndexNormalized)
                );
                float n2 = (float) noise.eval(
                        50 + slider("freq") * cos(angle),
                        50 + slider("freq") * sin(angle),
                        sin(t+ringIndexNormalized),
                        cos(t+ringIndexNormalized)
                );

                float x = r * cos(angle) + n0 * slider("x amp", 500);
                float z = r * sin(angle) + n1 * slider("z amp", 500);
                float y = n2 * slider("y amp", 500);
                stroke(100+ringIndexNormalized*155);
                vertex(x, y, z);
            }
            endShape();
        }
        gui();
    }
}
