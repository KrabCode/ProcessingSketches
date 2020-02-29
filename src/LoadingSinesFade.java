import applet.KrabApplet;
import ch.bildspur.postfx.builder.PostFX;
import processing.core.PGraphics;

@SuppressWarnings("DuplicatedCode")
public class LoadingSinesFade extends KrabApplet {
    PostFX fx;
    private PGraphics pg;
    float totalDuration = 600;

    public static void main(String[] args) {
        LoadingSinesFade.main("LoadingSinesFade");
    }

    public void settings() {
        size(800, 800, P2D);
//        fullScreen(P2D, 2);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        fx = new PostFX(this);
        pg = createGraphics(width, height, P2D);
        pg.smooth(16);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        frameRecordingDuration *= 2;
        timeSpeed *= .5f;
    }

    public void draw() {
        pg.beginDraw();
        pg.colorMode(HSB,1,1,1,1);
        pg.background(1);
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
        t %= PI;
        int detail = sliderInt("detail", 100);
        float baseRadius = slider("radius", 200);
        float baseStartAngle = slider("start angle");
        float baseEndAngle = slider("end angle", TAU);
        float bothAngles = slider("both angles");
        baseStartAngle += bothAngles;
        baseEndAngle += bothAngles;
        float angleRollIn = easeNorm(t, 0, slider("roll in end"), slider("angle in ease"));
        baseEndAngle = lerp(baseStartAngle, baseEndAngle, angleRollIn);
        float animatedRotation = min(easeNorm(t, slider("rotate start"), slider("rotate end"),slider("rotation ease")),
                1.f-easeNorm(t,slider("rotate end"), slider("rotate start"),slider("rotation ease")))
                ;
        float fadeout = 1.f-easeNorm(t, slider("fade out start"), slider("fade out end"), slider("fade ease"));

        int copies = sliderInt("copies", 2);
        for (int copy = 0; copy < copies; copy++) {
            float copySinOffset = map(copy, 0, copies-1, 0, TAU);
            float copyAngleOffset = slider("angle offset") * randomDeterministic(copy);
            pg.beginShape();
            pg.noFill();
            float startAngle = baseStartAngle + copyAngleOffset;
            float endAngle = baseEndAngle + copyAngleOffset;
            float colorNorm = randomDeterministic(copy+7);
            HSBA colorZero = picker("color 0");
            HSBA colorOne = picker("color 1");
            for (int i = 0; i < detail; i++) {
                float iNorm = clampNorm(i, 0, detail - 1);
                float angle = map(iNorm, 0, 1, startAngle, endAngle);
                float tips = easeInAndOut(iNorm, slider("tips width", .5f),
                        slider("tips transition"),.5f, slider("tips ease"));
                float animatedOffset = animatedRotation*TAU*slider("rotation count");
                float radiusOffset = slider("amp") * sin(copySinOffset - animatedOffset + angle * sliderInt(
                        "freq"));
                float r = baseRadius + radiusOffset;
                float weight = slider("weight",1.99f);
                pg.stroke(
                        lerp(colorZero.hue(), colorOne.hue(), colorNorm),
                        lerp(colorZero.sat(), colorOne.sat(), colorNorm),
                        lerp(colorZero.br(), colorOne.br(), colorNorm)
                        , min(tips, fadeout));
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
