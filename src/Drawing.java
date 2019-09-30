import applet.HotswapGuiSketch;
import codeanticode.tablet.Tablet;
import utils.OpenSimplexNoise;

import java.util.ArrayList;

public class Drawing extends HotswapGuiSketch {
    private Tablet tablet;

    private ArrayList<Line> lines = new ArrayList<Line>();
    private boolean wasMousePressed = false;
    private float t;
    private OpenSimplexNoise osn = new OpenSimplexNoise();

    public static void main(String[] args) {
        HotswapGuiSketch.main("Drawing");
    }

    public void settings() {
//        size(800, 800, P2D);
        fullScreen(P2D, 2);
    }

    public void setup() {
        tablet = new Tablet(this);
        surface.setAlwaysOnTop(true);
        background(20);
        stroke(255);
    }

    public void draw() {

        t += radians(1);
        boolean mouseJustPressed = !wasMousePressed && mousePressed;
        if (mouseJustPressed) {
            lines.add(new Line());
        }

        if (mousePressedOutsideGui && mouseX != 0 && mouseY != 0) {
            lines.get(lines.size() - 1).points.add(new Point(mouseX, mouseY, tablet.getPressure(), tablet.getTiltX(), tablet.getTiltY()));
        }
        wasMousePressed = mousePressed;

//        alphaFade(g);
        background(0);
        noFill();
        float freq = slider("freq", .1f);
        float range = slider("range", 10);
        float pow = slider("pow", 1);
        for (Line l : lines) {
            if (toggle("triangles")) {
                beginShape(TRIANGLE_STRIP);
            } else {
                beginShape();
            }
            for (Point p : l.points) {
                float a = (float) (2 * TWO_PI * osn.eval(p.x * freq, p.y * freq, t));
                float xOff = pow(p.pressure, pow) * range * cos(a);
                float yOff = pow(p.pressure, pow) * range * sin(a);
                strokeWeight(1);
                vertex(p.x, p.y);
                vertex(p.x + xOff, p.y + yOff);
            }
            endShape();
        }

        if (button("erase all")) {
            lines.clear();
        }

        if (toggle("eraser")) {
            float strength = slider("eraser strength", 50);
            if (mousePressed) {
                for (Line l : lines) {
                    ArrayList<Point> psToRemove = new ArrayList<>();
                    for (Point p : l.points) {
                        float d = dist(p.x, p.y, mouseX, mouseY);
                        if (d < strength) {
                            psToRemove.add(p);
                        }
                    }
                    l.points.removeAll(psToRemove);
                }
            }
        }

        rec();
        gui();
    }

    static class Line {
        ArrayList<Point> points = new ArrayList<Point>();
    }

    static class Point {
        float x, y, pressure, tiltX, tiltY;

        Point(float x, float y, float pressure, float tiltX, float tiltY) {
            this.x = x;
            this.y = y;
            this.pressure = pressure;
            this.tiltX = tiltX;
            this.tiltY = tiltY;
        }
    }
}
