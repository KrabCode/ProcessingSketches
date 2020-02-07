import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-06
 */
public class Change extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Change");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.colorMode(HSB,1,1,1,1);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        group("global");
        pg.background(picker("background").clr());
        float animation = norm(t % TWO_PI, 0, TWO_PI);
        drawArc(animation);
        drawTriangle(animation);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void drawTriangle(float animation) {
        group("triangle");
        float a = slider("angle");;
        float w = slider("w");
        float a0 = a-w*.5f;
        float a1 = a+w*.5f;
        float aMiddle = lerp(a0, a1, .5f);
        float r0 = slider("r 0");
        float r1 = slider("r 1");
        pg.pushMatrix();
        pg.translate(width*.5f, height*.5f);
        pg.noStroke();
        pg.fill(picker("fill").clr());
        pg.beginShape();
        pg.vertex(r0*cos(a0), r0*sin(a0));
        pg.vertex(r1*cos(aMiddle), r1*sin(aMiddle));
        pg.vertex(r0*cos(a1), r0*sin(a1));
        pg.endShape();
        pg.popMatrix();
    }

    private void drawArc(float animation) {
        group("arc");
        float r = slider("radius", 200);
        float w = slider("weight", 5);
        float start = slider("angle 0", PI);
        float end = slider("angle 1", 0);
        if (animation < .5f) {
            float eased = ease(animation*2, slider("move ease"));
            end = map(eased, 0, 1, start, end);
        } else {
            float eased = ease((animation-.5f)*2, slider("move ease"));
            start = map(eased, 0, 1, start, end);
        }
        int detail = sliderInt("detail", 2000);
        int hairs = sliderInt("hairs", 20);
        pg.pushMatrix();
        pg.translate(width * .5f, height * .5f);
        HSBA fill = picker("fill");
        pg.noStroke();
        pg.fill(fill.hue(), fill.sat(), fill.br(), fill.alpha()*easeInAndOut(animation,
                slider("alpha w", .5f),
                slider("alpha transition", .1f),
                slider("alpha c", .5f),
                slider("alpha ease", 1)));
        for (int i = 0; i < hairs; i++) {
            float rOff = slider("r off", 1)*sin(i*17.325f);
            float wOff = slider("w off", 1)*abs(sin(i*30.1423f));
            float aOff = slider("a off") * sin(i*21.1214f);
            pg.beginShape(TRIANGLE_STRIP);
            for (int j = 0; j < detail; j++) {
                float inorm = constrain(norm(j, 0, detail), 0, 1);
                float a = map(inorm, 0, 1, start+aOff, end+aOff);
                float r0 = r+rOff - (w+wOff) * .5f;
                float r1 = r+rOff + (w+wOff) * .5f;
                pg.vertex(r0 * cos(a), r0 * sin(a));
                pg.vertex(r1 * cos(a), r1 * sin(a));
            }
            pg.endShape();
        }
        pg.popMatrix();
    }
}
