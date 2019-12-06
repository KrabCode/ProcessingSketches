import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Parametric extends KrabApplet {
    private PGraphics pg;
    private float r, h;
    private float easing;

    public static void main(String[] args) {
        Parametric.main("Parametric");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        pg.translate(width * .5f, height * .5f);
        PVector rot = sliderXYZ("rotation");
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z + t);
        drawParametrically();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void drawParametrically() {
        int uMax = sliderInt("u", 1, 1000, 10);
        int vMax = sliderInt("v", 1, 1000, 10);
        r = slider("radius", 10);
        h = r * (1 + slider("height", 0));
        easing = slider("easing", 1);
        pg.strokeWeight(slider("weight", 1));
        pg.stroke(picker("stroke", 1).clr());
        pg.fill(picker("fill", 1).clr());
        for (int uIndex = 0; uIndex <= uMax; uIndex++) {
            if (toggle("points")) {
                pg.beginShape(POINTS);
            } else {
                pg.beginShape(TRIANGLE_STRIP);
            }
            for (int vIndex = 0; vIndex <= vMax; vIndex++) {
                float u0 = norm(uIndex, 0, uMax);
                float u1 = norm(uIndex + 1, 0, uMax);
                float v = norm(vIndex, 0, vMax);
                PVector a = getVector(u0, v);
                pg.vertex(a.x, a.y, a.z);
                if (!toggle("points")) {
                    PVector b = getVector(u1, v);
                    pg.vertex(b.x, b.y, b.z);
                }
            }
            pg.endShape();
        }
    }

    private PVector getVector(float u, float v) {
        String option = options("russian", "catenoid", "screw", "hexaedron", "moebius", "multitorus");
        if (option.equals("russian")) {
            return russianRoof(u, v);
        } else if (option.equals("catenoid")) {
            return catenoid(u, v);
        } else if (option.equals("screw")) {
            return screw(u, v);
        } else if (option.equals("hexaedron")) {
            return hexaedron(u, v);
        } else if (option.equals("moebius")) {
            return moebius(u, v);
        }
        return new PVector();
    }
    private PVector moebius(float u, float v) {
        u = -.4f + u * .8f;
        v = v * TWO_PI;
        return new PVector(
                r * (cos(v) + u * cos(v / 2) * cos(v)),
                r * (sin(v) + u * cos(v / 2) * sin(v)),
                h * (u * sin(v / 2))
        );
    }

    private PVector hexaedron(float u, float v) {
        u = -1.3f + u * 2.6f;
        v = v * TWO_PI;
        return new PVector(
                r * pow(cos(v), 3) * pow(cos(u), 3),
                r * pow(sin(v), 3) * pow(cos(u), 3),
                h * pow(sin(u), 3)
        );
    }

    private PVector screw(float u, float v) {
        u = u * 12.4f;
        v = v * 2;
        return new PVector(
                r * cos(u) * sin(v),
                r * sin(u) * sin(v),
                h * ((cos(v) + log(tan(v / 2f))) + 0.2f * u)
        );
    }

    private PVector catenoid(float u, float v) {
        u = PI - u * TWO_PI;
        v = PI - v * TWO_PI;
        return new PVector(
                r * 2 * cosh(v / 2) * cos(u),
                r * 2 * cosh(v / 2) * sin(u),
                h * v
        );
    }

    private PVector russianRoof(float u, float v) {
        u = u * TWO_PI;
        return new PVector(
                (r - r * ease(v, easing)) * cos(u),
                (r - r * ease(v, easing)) * sin(u),
                (-1 + 2 * v) * h
        );
    }

    private float cosh(float n) {
        return (float) Math.cosh(n);
    }
}
