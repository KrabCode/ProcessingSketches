import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class DotSpiral extends KrabApplet {
    private float eulersNumber = 1.618282f;
    private PGraphics pg;
    private PVector center;
    private float a = 0;

    public static void main(String[] args) {
        DotSpiral.main("DotSpiral");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.background(0);
        pg.endDraw();
        center = new PVector(width * .5f, height * .5f);
//        frameRecordingDuration *= 2;
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        drawGrid();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void drawGrid() {
        group("grid");
        int count = sliderInt("count");
        float margin = slider("margin");
        for (int xi = 0; xi < count; xi++) {
            for (int yi = 0; yi < count; yi++) {
                PVector pos = new PVector(
                        map(xi, 0, count - 1, -margin, width + margin),
                        map(yi, 0, count - 1, -margin, height + margin));
                float distanceToCenter = PVector.dist(center, pos);
                float theta = atan2(pos.y - center.y, pos.x - center.x) + slider("spiral speed") * t;
                PVector offset = new PVector(slider("amp")*distanceToCenter, 0);
                float rotation = slider("rotate speed", 1) * t +
                                sin(slider("theta frequency") * theta +
                                (slider("distance") * distanceToCenter));
                offset.rotate(rotation);
                pos.add(offset);
                float strokeNorm = .5f+.5f*sin(t*slider("stroke speed") +
                        (distanceToCenter+rotation)*slider("stroke distance"));
                pg.strokeWeight(map(strokeNorm, 0, 1, slider("min weight"), slider("max weight")));
                pg.stroke(map(strokeNorm, 0, 1, slider("min stroke"), slider("max stroke")));
                pg.point(pos.x, pos.y);
            }
        }
    }
}
