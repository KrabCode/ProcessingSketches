import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2020-01-11
 */
public class SciFiDisplay extends KrabApplet {
    private ArrayList<Float> noiseHistory = new ArrayList<>();
    private PGraphics pg;
    private HSBA bgFill;
    private HSBA fgFill;
    private float rectRound;

    public static void main(String[] args) {
        KrabApplet.main("SciFiDisplay");
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
        frameRecordingDuration *= 3;
    }

    public void draw() {
        group("global");
        bgFill = picker("bg fill", 0, .5f);
        fgFill = picker("fg fill", 1, 1);
        rectRound = slider("rect round", 10);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        backgroundPass(pg);
        group("parametric");
        pg.hint(ENABLE_DEPTH_TEST);
        drawParametric(pg);
        pg.hint(DISABLE_DEPTH_TEST);
        group("noise track");
        noiseTracker(pg, slider("x", 20), slider("y", 20), slider("w", 200), slider("h", 200));
        group("noise bars a");
        noiseBars(pg, slider("x", 20), slider("y", height - 220), slider("w", 200), slider("h", 200), slider("i " +
                "mult", 10));
        group("noise bars b");
        noiseBars(pg, slider("x", 20), slider("y", height - 220), slider("w", 200), slider("h", 200), slider("i " +
                "mult", 10));
        group("noise map");
        noiseMap(pg, slider("x", 20), slider("y", height - 220), slider("w", 200), slider("h", 200));
        group("lissajous");
        drawLissajous(pg, slider("x", 20), slider("y", height - 220),
                slider("r", 200), slider("x freq", 1), slider("y freq", 2));
        group("bottom shader");
        bottomShaderPass(pg, slider("x", 20), slider("y", height - 220), slider("w", 200), slider("h", 200));
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void noiseMap(PGraphics pg, float x, float y, float w, float h) {
//        componentBackground(pg, x, y, w, h);
        String noiseMapShader = "noise2D.glsl";
        uniform(noiseMapShader).set("res", w, h);
        uniform(noiseMapShader).set("time", t);
        hotShader(noiseMapShader, pg);
        pg.noStroke();
        pg.rectMode(CENTER);
        pg.rect(x + w * .5f, y + h * .5f, w, h, rectRound, rectRound, rectRound, rectRound);
        pg.resetShader();
    }


    void drawLissajous(PGraphics pg, float x, float y, float r, float xFreq, float yFreq) {
        float padding = slider("padding", 20);
        componentBackground(pg, x - r - padding, y - r - padding, r * 2 + padding * 2, r * 2 + padding * 2);
        ArrayList<PVector> points = new ArrayList<PVector>();
        float count = slider("count", 300);
        count = max(1, count);
        for (int i = 0; i < count; i++) {
            float inorm = norm(i, 0, count - 1);
            float ix = r * cos(inorm * TWO_PI * xFreq + t);
            float iy = r * sin(inorm * TWO_PI * yFreq + t);
            points.add(new PVector(x + ix, y + iy, inorm));
        }
        pg.stroke(fgFill.clr());
        pg.beginShape(LINES);
        pg.strokeWeight(1);
        for (PVector a : points) {
            for (PVector b : points) {
                if (a.equals(b)) {
                    continue;
                }
                float d = dist(a.x, a.y, b.x, b.y);
                float w = pow(1 / ((d / r) + 1), slider("distance", 12));
                pg.stroke(fgFill.hue(), fgFill.sat(), fgFill.br(), w);
                pg.vertex(a.x, a.y);
                pg.vertex(b.x, b.y);
            }
        }
        pg.endShape();
    }

    private void noiseTracker(PGraphics pg, float x, float y, float w, float h) {
        componentBackground(pg, x, y, w, h);
        pg.noFill();
        pg.stroke(fgFill.clr());
//        grid(pg, x, y, w, h);
        pg.stroke(picker("noise").clr());
        pg.strokeWeight(slider("noise w"));
        int noiseHistorySize = sliderInt("noise history size");
        while (noiseHistory.size() > noiseHistorySize) {
            noiseHistory.remove(0);
        }
        noiseHistory.add(fbm(t));
        pg.beginShape();
        for (int i = 0; i < noiseHistory.size(); i++) {
            float inorm = norm(i, 0, noiseHistory.size() - 1);
            float val = noiseHistory.get(i);
            float ix = x + inorm * w;
            float iy = y + h * val;
            pg.vertex(ix, iy);
        }
        pg.endShape();
    }

    float fbm(float x) {
        float value = 0.f;
        float amplitude = slider("fbm amp", 1);
        float frequency = slider("fbm freq", 10);
        for (int i = 0; i < 4; i++) {
            float n = 1 - 2 * noise(x * frequency);
            value += amplitude * n;
            amplitude *= slider("fbm amp mult", 0.5f);
            frequency *= slider("fbm freq mult", 2);
        }
        return .5f + .5f * value;
    }

    private void componentBackground(PGraphics pg, float x, float y, float w, float h) {
        pg.noStroke();
        pg.fill(bgFill.clr());
        pg.rectMode(CORNER);
        pg.rect(x, y, w, h, rectRound, rectRound, rectRound, rectRound);
    }

    private void noiseBars(PGraphics pg, float x, float y, float w, float h, float iMult) {
        componentBackground(pg, x, y, w, h);
//        grid(pg, x, y, w, h);
        float padding = slider("padding", 10);
        int barCount = sliderInt("count", 2);
        float bw = w / barCount - padding;
        for (int i = 0; i < barCount; i++) {
            float n = noise(t + i * iMult);
            float bh = n * (h - padding * 2);
            pg.noStroke();
            pg.fill(fgFill.clr());
            pg.rectMode(CENTER);
            float bx = map(i, 0, barCount - 1, x + padding + bw / 2, x + w - padding - bw / 2);
            pg.rect(bx, y + h * .5f, bw, bh, rectRound, rectRound, rectRound, rectRound);
        }
    }

    private void grid(PGraphics pg, float x, float y, float w, float h) {
        pg.stroke(fgFill.clr());
        pg.strokeWeight(1);
        PVector gridSize = new PVector(sliderInt("grid x"), sliderInt("grid y"));
        for (int xi = 0; xi < gridSize.x; xi++) {
            float gridX = map(xi, 0, gridSize.x - 1, x, x + w);
            pg.line(gridX, y, gridX, y + h);
        }
        for (int yi = 0; yi < gridSize.y; yi++) {
            float gridY = map(yi, 0, gridSize.y - 1, y, y + h);
            pg.line(x, gridY, x + w, gridY);
        }
    }

    private void backgroundPass(PGraphics pg) {
        String sciFiBg = "sciFiBackground.glsl";
        uniform(sciFiBg).set("time", t);
        hotFilter(sciFiBg, pg);
    }


    private void bottomShaderPass(PGraphics pg, float x, float y, float w, float h) {
        String bottomShader = "sciFiBottom.glsl";
        uniform(bottomShader).set("res", w, h);
        uniform(bottomShader).set("time", t);
        hotShader(bottomShader, pg);
        pg.rectMode(CENTER);
        pg.noStroke();
        pg.rect(x + w * .5f, y + h * .5f, w, h, rectRound, rectRound, rectRound, rectRound);
        pg.resetShader();
    }
}
