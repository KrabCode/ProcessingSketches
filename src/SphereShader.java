import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PShape;

public class SphereShader extends HotswapGuiSketch {
    PGraphics pg;
    PShape sphere;
    private float t;
    private String sphereShader = "sphere.glsl";

    public static void main(String[] args) {
        HotswapGuiSketch.main("SphereShader");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(500, 500, P2D);
        sphere = createSphere(250, pg);
    }

    public void draw() {
        t += radians(slider("time"));

        pg.beginDraw();
        hotFilter(sphereShader,pg);
        pg.endDraw();
        sphere.setTexture(pg);

        background(100);
        translate(width * .5f, height * .5f);
        rotateY(t);
        shape(sphere);

        gui();
    }

    PShape createSphere(float r, PGraphics pg) {
        PShape sphere = createShape(SPHERE, r);

        sphere.setStroke(false);
        return sphere;
    }


}
