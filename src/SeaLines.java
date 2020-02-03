import applet.KrabApplet;
import ch.bildspur.postfx.builder.PostFX;
import processing.core.PGraphics;
import processing.core.PVector;

public class SeaLines extends KrabApplet {
    private float diameter = 300;
    private PGraphics pg;
    private PVector center;
    PostFX fx;

    public static void main(String[] args) {
        SeaLines.main("SeaLines");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        fx = new PostFX(this);
        surface.setAlwaysOnTop(true);
        center = new PVector(width * .5f, height * .5f);
        pg = createGraphics(width, height, P2D);
        pg.smooth(16);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        group("global");
        pg.beginDraw();
        if (frameCount < 5) {
            pg.background(0);
        }
        alphaFade(pg);
        drawBorder();
        drawSea();
        group("bloom");
        pg.endDraw();
        fx.render(pg).bloom(slider("threshold"), sliderInt("blur size"), slider("sigma")).compose(pg);
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void drawBorder() {
        group("border");
        diameter = slider("diameter", 300);
        pg.noFill();
        pg.strokeWeight(slider("weight"));
        pg.stroke(picker("stroke").clr());
        pg.ellipse(center.x, center.y, diameter, diameter);
    }

    private void drawSea() {
        group("lines");
        float yStep = slider("y step", 10);
        float horizonY = slider("y", center.y);
        float radius = diameter * .5f;
        for (float baseY = horizonY; baseY < center.y + radius; baseY += yStep) {
            float w = 2 * sqrt(pow(radius, 2) - pow(abs(center.y - baseY), 2));
            pg.strokeWeight(slider("weight"));
            pg.stroke(picker("stroke").clr());
            float leftX = center.x - w*.5f - slider("margin");
            float rightX = center.x + w*.5f + slider("margin");
            int detail = sliderInt("detail", 100);
            pg.beginShape();
            for (int i = 0; i < detail; i++) {
                float inorm = constrain(norm(i, 0, detail - 1), 0, 1);
                float x = lerp(leftX, rightX, inorm);
                float y = baseY + getWaveAt(x, baseY);
                if (dist(x, y, center.x, center.y) > radius) {
                    continue;
                }
                pg.vertex(x, y);
            }
            pg.endShape();
        }
    }

    private float getWaveAt(float x, float y) {
        int octaves = sliderInt("octaves", 1);
        PVector freq = sliderXY("freq").copy();
        float amp = slider("amp");
        float sum = 0;
        for (int i = 0; i < octaves; i++) {
            sum += amp*sin(x*freq.x + y*freq.y + t);
            amp *= slider("amp mult", .5f);
            freq.mult(slider("freq mult", 2));
            x += 7.1238;
            y += 9.1456;
        }
        group("lines");
        return sum;
    }

}
