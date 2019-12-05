import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class SpeedDrawing extends KrabApplet {
    int currentColor = 0;
    float currentWeight = 0;
    private PGraphics pg;
    private ArrayList<Line> lines = new ArrayList<Line>();

    public static void main(String[] args) {
        SpeedDrawing.main("SpeedDrawing");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        reset();
    }

    public void draw() {
        if (button("clear")) {
            reset();
        }
        pg.beginDraw();
        alphaFade(pg);
        pg.translate(0, 0);
        currentColor = picker("stroke", 1).clr();
        currentWeight = slider("weight", 1);
        updateLines();
        pg.endDraw();
        background(0);
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void updateLines() {
        if (mousePressedOutsideGui) {
            lines.get(lines.size() - 1).points.add(new PVector(mouseX, mouseY));
        }
        for (Line line : lines) {
            line.update();
        }
    }

    public void keyReleased() {
        super.keyReleased();
        if (key == 'b') {
            removeLastLine();
        }
    }

    private void removeLastLine() {
        if (!lines.isEmpty()) {
            lines.remove(lines.size() - 1);
        }
    }

    private void reset() {
        lines.clear();
    }

    public void mousePressed() {
        lines.add(new Line(currentColor, currentWeight));
    }

    class Line {
        float i = 0;
        int clr;
        float weight;
        ArrayList<PVector> points = new ArrayList<PVector>();
        private float speed = 1;

        Line(int clr, float weight) {
            this.clr = clr;
            this.weight = weight;
            this.speed = speed;
        }

        void update() {
            if (points.isEmpty()) {
                return;
            }
            if (toggle("debug")) {
                pg.stroke(1);
                pg.strokeWeight(1);
                for (PVector p : points) {
                    pg.point(p.x, p.y);
                }
            }
            i += speed;
            i %= points.size();
            if (floor(i) < 1) {
                return;
            }
            PVector current = points.get(floor(i));
            PVector prev = points.get(floor(i) - 1);
            pg.stroke(clr);
            pg.strokeWeight(weight);
            pg.line(current.x, current.y, prev.x, prev.y);
        }
    }
}
