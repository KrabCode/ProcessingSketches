import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Parametric extends KrabApplet {
    private PGraphics pg;
    private float r;

    public static void main(String[] args) {
        Parametric.main("Parametric");
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
        pg.translate(width * .5f, height * .5f);
        PVector rot = sliderXYZ("rotation").add(sliderXYZ("rotation speed"));
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z);
        drawParametrically();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void drawParametrically() {
        r = slider("radius", 10);
        pg.colorMode(HSB, 1, 1, 1, 1);
        int uMax = sliderInt("u", 1, 1000, 10);
        int vMax = sliderInt("v", 1, 1000, 10);

        pg.strokeWeight(1);
        pg.stroke(picker("stroke", 1).clr());
        pg.fill(picker("fill", 1).clr());
        for (int uIndex = 0; uIndex <= uMax; uIndex++) {
            pg.beginShape(TRIANGLE_STRIP);
            for (int vIndex = 0; vIndex <= vMax; vIndex++) {
                float u0 = norm(uIndex, 0, uMax);
                float u1 = norm(uIndex+1, 0, uMax);
                float v0 = norm(vIndex, 0, vMax);
                pg.vertex(x(u0, v0), y(u0, v0), z(u0, v0));
                pg.vertex(x(u1, v0), y(u1, v0), z(u1, v0));
            }
            pg.endShape();
        }
    }

    private float x(float u, float v) {
        return (r - r*ease(v, slider("pow", 1))) * cos(u * TWO_PI);
    }

    private float y(float u, float v) {
        return (r - r*ease(v, slider("pow", 1))) * sin(u * TWO_PI);
    }

    private float z(float u, float v) {
        return (-1 + 2 * v) * r * slider("height");
    }
}
