import applet.GuiSketch;
import processing.core.PVector;
import processing.opengl.PShader;

public class LogSpiral extends GuiSketch {
    private float t;

    private float e = 2.71828182845904523536028747135266249775724709369995f;
    int framesToCapture = 360;
    int saveEnds = -1;
    private PShader rgbSplit;

    public static void main(String[] args) {
        GuiSketch.main("LogSpiral");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {

        rgbSplit = loadShader("rgbSplit.glsl");
    }

    public void draw() {
        framesToCapture = floor(slider("frames", 600));
        t = (toggle("forward")?-1:1)  * map(frameCount, 0, framesToCapture, 0, TWO_PI);
        t %= TWO_PI;

        background(0);
        translate(width * .5f, height * .5f);
        noFill();
        stroke(255);
        strokeWeight(slider("weight", 0, 3));
        int armCount = floor(slider("arm count", 1, 10));
        int count = floor(slider("point count", 500));
        float maxAngle = slider("max angle", 6);
        beginShape(LINES);
        for (int arm = 0; arm < armCount; arm++) {
            float normalArm = norm(arm, 0, armCount - 1);
            float normalArmNext = norm(arm - 1, 0, armCount - 1);
            pushMatrix();
            for (int i = 0; i < count; i++) {
                float a = slider("a");
                float b = slider("b", 2);
                float ni = norm(i, 0, count - 1);
                PVector v0 = getPosOnLogSpiral(ni, normalArm, a, b, maxAngle);
                PVector v1 = getPosOnLogSpiral(ni, normalArmNext, a, b, maxAngle);
                vertex(v0.x, v0.y);
                vertex(v1.x, v1.y);
            }
            popMatrix();
        }
        endShape();

        rgbSplit.set("strength", slider("strength", .02f));
        rgbSplit.set("easing", slider("easing", 2));
        filter(rgbSplit);

        if (frameCount <= saveEnds) {
            saveFrame(captureDir + "####.jpg");
        }

        gui();
    }

    public void keyPressed() {
        saveEnds = frameCount + framesToCapture + 1;
    }

    private PVector getPosOnLogSpiral(float ni, float armi, float a, float b, float maxAngle) {
        float angle = ni * TWO_PI * maxAngle;
        float r = pow(a * e, b * angle);
        angle += armi * TWO_PI;
        angle += t;
        return new PVector(r * cos(angle), r * sin(angle));
    }
}
