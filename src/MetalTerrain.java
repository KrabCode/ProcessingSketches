import applet.HotswapGuiSketch;
import applet.KrabApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import utils.OpenSimplexNoise;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-21
 */
@SuppressWarnings("DuplicatedCode")
public class MetalTerrain extends KrabApplet {
    private PGraphics pg;
    private OpenSimplexNoise noise = new OpenSimplexNoise();

    public static void main(String[] args) {
        KrabApplet.main("MetalTerrain");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        PVector translate = sliderXYZ("translate");
        pg.translate(width*.5f+translate.x, height*.5f+translate.y, translate.z);
        drawTorus();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void drawTorus() {
        pg.stroke(255);
        pg.fill(0);
        int xcount = sliderInt("x count");
        int zcount = sliderInt("y count");
        float baseRadius = slider("radius", 300);
        float length = slider("length", 300);
        for (int xi = 0; xi < xcount; xi++) {
            pg.beginShape(TRIANGLE_STRIP);
            for (int zi = 0; zi < zcount; zi++) {
                float theta = map(xi, 0, xcount-1, 0, TWO_PI);
                float nextTheta = map(xi+1, 0, xcount-1, 0, TWO_PI);
                float z = -map(zi, 0, zcount-1, 0, length);
                float radius = baseRadius + getNoise(xi, zi);
                float nextRadius = baseRadius + getNoise(xi+1, zi);
                pg.vertex(radius*cos(theta),radius*sin(theta),z);
                pg.vertex(nextRadius*cos(nextTheta),nextRadius*sin(nextTheta),z);
            }
            pg.endShape();
        }
    }


    private float getNoise(float theta, float z) {
        float amp = slider("amp", 100);
        float thetaFreq = sliderInt("theta freq", 1);
        float thetaRange = slider("theta range", 1);
        return (float) (amp*noise.eval(
                        thetaRange*cos(theta*thetaFreq),
                        thetaRange*sin(theta*thetaFreq),
                        z*slider("z freq")+t*slider("z speed"),
                t*slider("time speed")));
    }
}
