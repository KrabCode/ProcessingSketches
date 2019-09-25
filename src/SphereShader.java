import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PShape;

public class SphereShader extends HotswapGuiSketch {
    PGraphics planetTexture, cloudTexture;
    PShape planetShape, cloudShape;
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("SphereShader");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        planetTexture = createGraphics(1000, 1000, P2D);
        cloudTexture = createGraphics(4000, 4000, P2D);
        planetShape = createSphere(250);
        cloudShape = createSphere(255);
    }

    public void draw() {
        t += radians(slider("time"));
        background(0);
        translate(width * .5f, height * .5f);
        updatePlanet();
        updateClouds();
        rec();
        gui();
    }


    private void updatePlanet() {
        pushMatrix();
        String planetShader = "planet\\planet.glsl";
        planetTexture.beginDraw();
        uniform(planetShader).set("time", t);
        hotFilter(planetShader, planetTexture);
        planetTexture.endDraw();
        planetShape.setTexture(planetTexture);
        shape(planetShape);
        popMatrix();
    }

    private void updateClouds() {
        String cloudShader = "planet\\clouds.glsl";
        cloudTexture.beginDraw();
        uniform(cloudShader).set("time", t);
        hotFilter(cloudShader, cloudTexture);
        cloudTexture.endDraw();
        cloudShape.setTexture(cloudTexture);
        pushMatrix();
        rotateY(t);
//        hint(DISABLE_DEPTH_TEST);
        shape(cloudShape);
//        hint(ENABLE_DEPTH_TEST);
        popMatrix();
    }

    PShape createSphere(float r) {
        PShape sphere = createShape(SPHERE, r);
        sphere.setStroke(false);
        return sphere;
    }
}
