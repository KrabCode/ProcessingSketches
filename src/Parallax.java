import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class Parallax extends KrabApplet {
    PVector avgSpeed;
    PVector shadowOffset = new PVector();
    ArrayList<Shape> shapes = new ArrayList<Shape>();
    HSBA minColor;
    HSBA maxColor;
    PGraphics pg;
    PVector minSize;
    PVector maxSize;

    public static void main(String[] args) {
        Parallax.main("Parallax");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        surface.setLocation(1920 - 820, 20);
        pg = createGraphics(width, height, P2D);
        pg.smooth(10);
        pg.colorMode(HSB, 1, 1, 1, 1);
    }

    public void draw() {
        minColor = picker("min color");
        maxColor = picker("max color");
        avgSpeed = sliderXY("speed");
        shadowOffset = sliderXY("shadow offset");
        pg.beginDraw();
        String parallaxSea = "parallaxSea.glsl";
        uniform(parallaxSea).set("time", t);
        hotFilter(parallaxSea, pg);
        updateLines();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    void updateLines() {
        int typeCount = sliderInt("type count", 1);
        int linesPerType = sliderInt("shapes per type", 1);
        if (shapes.size() != typeCount * linesPerType || button("regenerate")) {
            generateLines(typeCount, linesPerType);
        }
        for (Shape shape : shapes) {
            shape.update();
            shape.drawShadows(pg);
            shape.drawShape(pg);
        }
    }

    void generateLines(int typeCount, int linesPerType) {
        minSize = sliderXY("min size");
        maxSize = sliderXY("max size");
        shapes.clear();
        for (int typeIndex = 0; typeIndex < typeCount; typeIndex++) {
            float typeNorm = 1 - norm(typeIndex, -.5f, typeCount - 1);
            for (int lineIndex = 0; lineIndex < linesPerType; lineIndex++) {
                shapes.add(new Shape(typeNorm));
            }
        }
    }

    class Shape {
        float typeNorm;
        int color;
        PVector size = new PVector();
        PVector pos;
        PVector spd;

        Shape(float typeNorm) {
            this.typeNorm = typeNorm;
            this.pos = new PVector(random(width), random(height));
        }

        void drawShadows(PGraphics pg) {
            pg.pushMatrix();
            pg.translate(pos.x + shadowOffset.x, pos.y + shadowOffset.y);
            pg.rotate(spd.heading());
            pg.noStroke();
            pg.fill(0);
            pg.rectMode(CENTER);
            pg.rect(0, 0, size.x, size.y);
            pg.popMatrix();
        }

        void drawShape(PGraphics pg) {
            pg.pushMatrix();
            pg.translate(pos.x, pos.y);
            pg.rotate(spd.heading());
            pg.noStroke();
            pg.fill(color);
            pg.rectMode(CENTER);
            pg.rect(0, 0, size.x, size.y);
            pg.popMatrix();
        }

        void update() {
            size.x = map(typeNorm, 0, 1, minSize.x, maxSize.x);
            size.y = map(typeNorm, 0, 1, minSize.y, maxSize.y);
            color = lerpColor(minColor.clr(), maxColor.clr(), typeNorm);
            spd = getSpeed();
            pos.add(spd);
            constrainPosition();
        }

        PVector getSpeed() {
            return avgSpeed.copy().mult(1 - pow(typeNorm, slider("spd pow")));
        }

        void constrainPosition() {
            if (pos.x > width + size.x * 2) {
                pos.x -= width + size.x * 4;
            }
            if (pos.x < -size.x * 2) {
                pos.x += width + size.x * 4;
            }
            if (pos.y > height + size.y * 2) {
                pos.y -= height + size.y * 4;
            }
            if (pos.y < -size.y * 2) {
                pos.y += height + size.y * 4;
            }
        }
    }
}
