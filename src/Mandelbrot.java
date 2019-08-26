import applet.GuiSketch;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.opengl.PShader;


public class Mandelbrot extends GuiSketch {

    // TODO record higher resolution, blur and then render smaller video
    // TODO also try motion blur maybe

    private PShader mandelbrot;
    private PVector cameraPos;

    private float scale = 1;
    private float scaleTarget = 1;

    float t;
    private boolean recording;
    private float recStart;
    private float recEnd;
    private float recLength;
    private PGraphics canvas;

    public static void main(String[] args) {
        GuiSketch.main("Mandelbrot");
    }

    public void settings() {
//        fullScreen(P2D);
        size(800, 800, P2D);
    }

    public void setup() {
        canvas = createGraphics(width * 2, height * 2, P2D);
        mandelbrot = loadShader("mandelbrot.glsl");
        cameraPos = new PVector();
    }

    public void draw() {
        int timeMultiplier = floor(slider("time mult", .8f, 5));
        t += radians(1f/timeMultiplier);
        recLength = 360*timeMultiplier;
        mandelbrot.set("t", t);
        mandelbrot.set("scale", scale);
        mandelbrot.set("offset", cameraPos.x, cameraPos.y);
        mandelbrot.set("hueStart", slider("hueStart", 0, 5, .6f));
        mandelbrot.set("hueRange", slider("hueRange", 0, 5, .1f));
        mandelbrot.set("distortMag", slider("distort", 0, .1f, 0));
        mandelbrot.set("iter", map(2*abs(recLength/2-(frameCount%recLength)), 0, recLength, 0, slider("maxIter", 1000)));
        mandelbrot.set("brLimit", slider("brLimit", 0, 2, 0.2f));
        if (mousePressed) {
            cameraPos.x += (pmouseX - mouseX) * scale * (width / (float) height); //assuming width > height
            cameraPos.y -= (pmouseY - mouseY) * scale;
        }
        scale = lerp(scale, scaleTarget, .2f);

        canvas.beginDraw();
        canvas.filter(mandelbrot);
        canvas.endDraw();
        image(canvas, 0, 0, width, height);

        if (recEnd > frameCount) {
            saveFrame(captureDir + "####.jpg");
        }

        gui();
    }

    public void keyPressed() {
        recStart = frameCount;
        recEnd = frameCount + recLength;
    }

    public void mouseWheel(MouseEvent event) {
        float e = event.getCount();
        if (e < 0) {
            scaleTarget *= .9;
        } else {
            scaleTarget *= 1.1;
        }
    }
}
