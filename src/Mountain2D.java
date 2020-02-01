import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-01
 */
public class Mountain2D extends KrabApplet {
    ArrayList<Line> lines = new ArrayList<>();
    private PGraphics pg;
    private float boundsRadius = 300;

    public static void main(String[] args) {
        KrabApplet.main("Mountain2D");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.smooth(16);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        frameRecordingDuration *= 2;
    }

    public void draw() {
        pg.beginDraw();
        if(frameCount < 5){
            pg.background(0);
        }
        pg.translate(width * .5f, height * .5f);
        updateLines();
        pg.stroke(picker("ellipse stroke").clr());
        pg.strokeWeight(slider("ellipse weight"));
        pg.noFill();
        pg.ellipse(0, 0, boundsRadius * 2, boundsRadius * 2);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    void updateLines() {
        float mountainTop = slider("top", -200);
        float mountainBottom = slider("bottom", 200);
        int count = sliderInt("count", 60);
        if (button("clear")) {
            lines.clear();
            pg.background(0);
        }

        if (lines.size() < count && frameCount % 3 == 0) {
            float ridgeFreq = slider("ridge freq");
            float ridgeAmp = slider("ridge amp");
            float y = map(lines.size(), 0, count, mountainTop, mountainBottom);
            float x = ridgeAmp * (1 - 2 * noise(t + y * ridgeFreq));
            lines.add(new Line(new PVector(x, y), TWO_PI + QUARTER_PI, TWO_PI));
            lines.add(new Line(new PVector(x, y), PI - QUARTER_PI, PI));
        }

        for (Line line : lines) {
            PVector origin = line.vertices.get(0);
            PVector end = line.vertices.get(line.vertices.size() - 1);
            float freq = slider("noise freq", .1f);
            float amp = slider("noise amp", .5f);
            float vertexSpeed = slider("vertex speed", 1);
            if (insideBounds(end)) {
                float n = amp * (1 - 2 * noise(end.x * freq, end.y * freq));
                float d = abs(origin.x - end.x);
                float dNorm = norm(d, 0, boundsRadius);
                float dEase = ease(dNorm, slider("slope ease", 1));
                if(Float.isNaN(dEase)){
                    dEase = 1;
                }
                float angle = map(dEase, 0, 1, line.slopeAngle, line.endAngle);
                PVector continuation = PVector.fromAngle(angle + n);
                continuation.mult(vertexSpeed);
                continuation = end.copy().add(continuation);
                pg.strokeWeight(slider("line weight", 1));
                pg.stroke(255);
                pg.line(end.x, end.y, continuation.x, continuation.y);
                line.vertices.add(continuation);
            }
        }
    }

    boolean insideBounds(PVector p) {
        return dist(p.x, p.y, 0, 0) < boundsRadius;
    }

    class Line {
        ArrayList<PVector> vertices = new ArrayList<PVector>();
        float slopeAngle, endAngle;

        Line(PVector origin, float slopeAngle, float endAngle) {
            vertices.add(origin);
            this.slopeAngle = slopeAngle;
            this.endAngle = endAngle;
        }
    }
}
