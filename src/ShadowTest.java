import applet.ShadowGuiSketch;
import applet.GuiSketch;
import peasy.PeasyCam;
import processing.core.PGraphics;

public class ShadowTest extends ShadowGuiSketch {
    public static void main(String[] args) {
        GuiSketch.main("ShadowTest");
    }

    float t = 0;
    PeasyCam cam;

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        super.setup(1024*4);
        cam = new PeasyCam(this, 150);
    }

    public void draw() {
        pushMatrix();
        translate(0, 30, 0);
        super.draw();
        popMatrix();
        t += radians(.5f);
        gui();
    }

    public void setLightDir() {
        lightDir.set(50 * cos(t), 50 * sin(t), 20);
    }

    public void background() {
        background(0);
    }

    public void animate(PGraphics canvas) {
        canvas.fill(150);
        canvas.noStroke();
        canvas.box(200, 1, 200);
        canvas.fill(255);
        float res = slider("detail", 20);
        float footprint = slider("footprint", 200);
        float logicalCenter = (res) / 2;
        float maxDist = res * .5f;
        float boxSize = (footprint / res) / 2;
        for (int xIndex = 0; xIndex <= res; xIndex++) {
            for (int yIndex = 0; yIndex <= res; yIndex++) {
                float x = map(xIndex, 0, res, -footprint * .5f, footprint * .5f);
                float y = map(yIndex, 0, res, -footprint * .5f, footprint * .5f);
                float d = map(dist(xIndex, yIndex, logicalCenter, logicalCenter), 0, maxDist, 0, 1);
                d = abs(sin(5*d));
                float boxHeight = 5 + d * boxSize * 2;
                canvas.pushMatrix();
                canvas.translate(x, -boxHeight/2, y);
                canvas.box(boxSize, boxHeight, boxSize);
                canvas.popMatrix();
            }
        }
    }
}
