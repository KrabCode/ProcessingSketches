import applet.GuiSketch;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.opengl.PShader;


public class Mandelbrot extends GuiSketch {

    // TODO record higher resolution, blur and then render smaller video
    // TODO also try motion blur maybe

    private PShader mandelbrot;
    private PVector offset;
    private float scale = 1;
    float t;
    private boolean recording;
    private float recStart;
    private float recEnd;
    private float recLength;

    public static void main(String[] args) {
        GuiSketch.main("Mandelbrot");
    }

    public void settings() {
//        fullScreen(P2D);
        size(800, 800, P2D);
    }

    public void setup() {
        mandelbrot = loadShader("mandelbrot.glsl");
        mandelbrot.set("palette", loadImage("palette.png"));
        offset = new PVector();
    }

    public void draw() {
        mandelbrot.set("distortMag", slider("distort",0,.1f, 0));
        t += radians(1);
        recLength = 360/slider("t");

        mandelbrot.set("scale", scale);
        mandelbrot.set("offset", offset.x, offset.y);
        mandelbrot.set("hueStart",slider("hueStart", 5));
        mandelbrot.set("hueRange",slider("hueRange", 5));

        float iter = slider("iterStart", 1000) + slider("iterRange", 1000) * sin(t);
        mandelbrot.set("t", t);
        mandelbrot.set("iter", iter);

        if (mousePressed) {
            offset.x += (pmouseX - mouseX)*scale;
            offset.y -= (pmouseY - mouseY)*scale;
        }
        filter(mandelbrot);

        if(recEnd > frameCount){
            saveFrame(captureDir+"####.jpg");
        }

        gui();

    }

    public void keyPressed(){
        recStart = frameCount;
        recEnd = frameCount + recLength;
    }

    public void mouseWheel(MouseEvent event) {
        float e = event.getCount();
        if(e < 0){
            scale *= .9;
        }else{
            scale *= 1.1;
        }
    }
}
