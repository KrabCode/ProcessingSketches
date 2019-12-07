import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Parametric extends KrabApplet {
    float mtTime = 0;
    private PGraphics pg;
    private float r, h;

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
        frameRecordingDuration *= 2f;
    }

    public void draw() {
        pg.beginDraw();
        if(toggle("raymarch")){

            String raymarch = "raymarch.glsl";
            uniform(raymarch).set("time", t * 3);
            hotFilter(raymarch, pg);
        }
        group("matrix");
        PVector translate = sliderXYZ("translate");
        pg.translate(translate.x + width * .5f, translate.y + height * .5f, translate.z);
        PVector rot = sliderXYZ("rotation");
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z + (toggle("z rotation") ? t : 0));
        drawParametrically();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void drawParametrically() {
        group("params");
        int uMax = sliderInt("u", 1, 1000, 10);
        int vMax = sliderInt("v", 1, 1000, 10);
        r = slider("radius", 10);
        h = r * (1 + slider("height", 0));
        pg.strokeWeight(slider("weight", 1));
        pg.stroke(picker("stroke", 1).clr());
        pg.fill(picker("fill", 1).clr());
        for (int uIndex = 0; uIndex <= uMax; uIndex++) {
            group("params");
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
        String option = options("russian", "catenoid", "screw", "hexaedron", "moebius",
                "torus", "multitorus", "helicoidal", "ufo", "sphere", "newShape");
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
        } else if (option.equals("torus")) {
            return torus(u, v);
        } else if (option.equals("multitorus")) {
            return multitorus(u, v);
        } else if (option.equals("helicoidal")) {
            return helicoidal(u, v);
        } else if (option.equals("ufo")) {
            return ufo(u, v);
        } else if (option.equals("sphere")) {
            return sphere(u, v);
        } else if (option.equals("newShape")) {
            return newShape(u, v);
        }
        return new PVector();
    }

    private PVector newShape(float u, float v) {

        return new PVector(
                u,
                v
        );
    }

    private PVector sphere(float u, float v) {
        u = -HALF_PI + u * PI;
        v = v * TWO_PI;
        return new PVector(
                r * cos(u) * cos(v),
                r * cos(u) * sin(v),
                h * sin(u)
        );
    }

    private PVector ufo(float u, float v) {
        u = -PI + u * TWO_PI;
        v = -PI + v * TWO_PI;
        return new PVector(
                r * (cos(u) / (sqrt(2) + sin(v))),
                r * (sin(u) / (sqrt(2) + sin(v))),
                h * (1 / (sqrt(2) + cos(v)))
        );
    }

    private PVector helicoidal(float u, float v) {
        u = -PI + u * TWO_PI;
        v = -PI + v * TWO_PI;
        return new PVector(
                r * (sinh(v) * sin(u)),
                r * (-sinh(v) * cos(u)),
                h * (3 * u)
        );
    }

    private PVector multitorus(float u, float v) {
        u = -PI + u * TWO_PI;
        v = -PI + v * TWO_PI;
        group("multitorus");
        mtTime = t * slider("time");
        float R3 = sliderInt("R3", 3);
        float R = sliderInt("R", 5);
        float N = sliderInt("N", 10);
        float N2 = sliderInt("N2", 4);
        group("params");
        return new PVector(
                r * (-sin(u) * multitorusF1(u - mtTime, v, N, R3, R, N2)),
                r * (cos(u) * multitorusF1(u - mtTime, v, N, R3, R, N2)),
                h * (multitorusF2(u - mtTime, v, N, R3, R, N2))
        );
    }

    private float multitorusF1(float u, float v, float N, float R3, float R, float N2) {
        return (R3 + (R / (10 * N)) * cos(N2 * u / N + ((R / (10 * N)) - R / 10) / (R / (10 * N)) * v) + (R / 10 - (R / (10 * N))) * cos(N2 * u / N + v));
    }

    private float multitorusF2(float u, float v, float N, float R3, float R, float N2) {
        return ((R / (10 * N)) * sin(N2 * u / N + ((R / (10 * N)) - R / 10) / (R / (10 * N)) * v) + (R / 10 - (R / (10 * N))) * sin(N2 * u / N + v));
    }

    private PVector torus(float u, float v) {
        u = u * TWO_PI;
        v = v * TWO_PI;
        return new PVector(
                r * ((1 + 0.5f * cos(u)) * cos(v)),
                r * ((1 + 0.5f * cos(u)) * sin(v)),
                h * (0.5f * sin(u))
        );
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
        group("russian roof");
        float easing = slider("easing", 1);
        group("params");
        return new PVector(
                (r - r * ease(v, easing)) * cos(u),
                (r - r * ease(v, easing)) * sin(u),
                (-1 + 2 * v) * h
        );
    }

    private float cosh(float n) {
        return (float) Math.cosh(n);
    }

    private float sinh(float n) {
        return (float) Math.sinh(n);
    }
}
