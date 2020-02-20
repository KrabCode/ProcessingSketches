import applet.KrabApplet;
import ch.bildspur.postfx.builder.PostFX;
import processing.core.PGraphics;

public class LoadingSines extends KrabApplet {
    PostFX fx;
    private PGraphics pg;

    public static void main(String[] args) {
        LoadingSines.main("LoadingSines");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        fx = new PostFX(this);
        pg = createGraphics(width, height, P2D);
        pg.smooth(16);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
//        frameRecordingDuration *= 2;
//        timeSpeed *= .5f;
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        pg.translate(width * .5f, height * .5f);
        drawCircle();
        pg.endDraw();
        image(pg, 0, 0);
        bloomPass();
        rec(g);
        gui();
    }

    void bloomPass() {
        fx.render().bloom(
                slider("threshold"),
                sliderInt("blur size"),
                slider("sigma")
        ).compose();
    }

    private void drawCircle() {
        int detail = sliderInt("detail", 100);
        float baseRadius = slider("radius", 200);
        float baseStartAngle = slider("start angle");
        float baseEndAngle = slider("end angle", PI);
        int copies = sliderInt("copies", 2);
        for (int copy = 0; copy < copies; copy++) {
            float copySinOffset = map(copy, 0, copies - 1, 0, TAU);
            float copyAngleOffset = slider("angle offset") * randomDeterministic(copy);
            pg.beginShape();
            pg.noFill();
            for (int i = 0; i < detail; i++) {
                float startAngle = baseStartAngle + copyAngleOffset + t;
                float endAngle = baseEndAngle + copyAngleOffset + t;
                float angle = map(i, 0, detail - 1, startAngle, endAngle);
                float angleNorm = clampNorm(angle, startAngle, endAngle);
                float fade = easeInAndOut(angleNorm, slider("fade width"),
                        slider("fade trans"),
                        slider("fade center"),
                        slider("fade ease"));
                float radiusOffset = slider("amp") * sin(t+copySinOffset + angle * sliderInt("freq"));
                float r = baseRadius + radiusOffset;
                float weight = 1.99f;
                pg.stroke(255, 255 * fade);
                pg.strokeWeight(weight);
                pg.vertex(r * cos(angle), r * sin(angle));
            }
            pg.endShape();
        }
    }

    private float randomDeterministic(float seed) {
        return abs(sin(seed * 323.121f) * 454.123f) % 1;
    }
}
