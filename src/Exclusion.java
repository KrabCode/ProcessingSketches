import applet.GuiSketch;
import processing.opengl.PShader;

public class Exclusion extends GuiSketch {
    private float tRecStart = -1;
    private float tRecFinish = -1;
    private float t;
    private PShader fxaa;

    public static void main(String[] args) {
        GuiSketch.main("Exclusion");
    }

    public void settings() {
//        size(800, 800, P2D);
        fullScreen(P2D);
    }

    public void setup() {
        fxaa = new PShader(this, "fxaaVert.glsl", "fxaaFrag.glsl");
        colorMode(HSB, 1, 1, 1, 1);
    }

    public void draw() {
        t += radians(1);
        background(0);
        noStroke();

        translate(width * .5f, height * .5f);
        int rings = floor(slider("r count", 120));
        for (int rIndex = 0; rIndex < rings; rIndex++) {
            float r = map(rIndex, 0, rings - 1, 0, width);
            int aCount = floor(slider("a count", 50));
            for (int aIndex = 0; aIndex < aCount; aIndex++) {
                if (toggle("ADD/SUB")) {
                    if (rIndex % 2 == 0) {
                        blendMode(ADD);
                    } else {
                        blendMode(SUBTRACT);
                    }
                } else {
                    blendMode(EXCLUSION);
                }
                float a = map(aIndex, 0, aCount, 0, TWO_PI);
                float x = r * cos(a);
                float y = r * sin(a);
                float d = (r / width) * slider("hue", 20);

                fill(d % 1, slider("saturation", 0, 1, 1), slider("brightness", 0, 1, .1f));

                float size = slider("radius", 1000);
                if (toggle("cycle")) {
                    size += slider("cycle start", 1000) + slider("cycle speed", 360) * sin(t);
                } else {
                    size += slider("linear speed", 20) * t;
                }
                ellipse(x, y, size, size);
            }
        }

        blendMode(BLEND);

        if (toggle("fxaa")) {
            for (int i = 0; i < slider("passes", 0, 30, 2); i++) {
                filter(fxaa);
            }
        } else {
            resetShader();
        }

        if (tRecStart > 0 && frameCount <= tRecFinish) {
            saveFrame(captureFilename);
        }

        gui();
    }

    public void keyPressed() {
        tRecStart = frameCount;
        tRecFinish = frameCount + 360;
    }

}
