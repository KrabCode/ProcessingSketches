import applet.GuiSketch;
import processing.core.PVector;

public class LogSpiral extends GuiSketch {

    float t = 0;
    private float saveStarts = 0;
    private float saveDuration = 0;
    private float armCount;
    private float e = 2.71828182845904523536028747135266249775724709369995f;
    private boolean keyWasPressed;


    public static void main(String[] args) {
        GuiSketch.main("LogSpiral");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {

    }

    public void draw() {
        boolean keyJustReleased = keyWasPressed && !keyPressed;
        keyWasPressed = keyPressed;
        if (keyJustReleased) {
            saveStarts = frameCount;
            saveDuration = 360;
        }

        t += radians(1);

        background(0);
        translate(width * .5f, height * .5f);
        noFill();
        stroke(255);
        strokeWeight(slider("weight", 0, 2));
        armCount = floor(slider("arm count", 5, 10));
        int count = floor(slider("point count", 500));
        float maxAngle = slider("max angle", 6);
        beginShape(LINES);
        for (int arm = 0; arm < armCount; arm++) {
            float normArm = norm(arm, 0, armCount - 1);
            float normArmNext = norm(arm - 1, 0, armCount - 1);
            pushMatrix();
            for (int i = 0; i < count; i++) {
                float a = slider("a");
                float b = slider("b", 2);
                float normIndex = norm(i, 0, count - 1);
                PVector v0 = getPosOnLogSpiral(normIndex, normArm, a, b, maxAngle);
                PVector v1 = getPosOnLogSpiral(normIndex, normArmNext, a, b, maxAngle);
                vertex(v0.x, v0.y);
                vertex(v1.x, v1.y);
            }
            popMatrix();
        }
        endShape();

        if (saveStarts > 0 && frameCount < saveStarts + saveDuration) {
            saveFrame(captureFilename);
        }

        gui();
    }

    private PVector getPosOnLogSpiral(float normIndex, float normArm, float a, float b, float maxAngle) {
        float angle = normIndex * TWO_PI * maxAngle;
        float r = pow(a * e, b * angle);
        angle += normArm * TWO_PI;
        angle -= t;
        return new PVector(r * cos(angle), r * sin(angle));
    }
}
