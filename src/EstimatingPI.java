import applet.KrabApplet;
import processing.core.PGraphics;

public class EstimatingPI extends KrabApplet {
    public static void main(String[] args) {
        EstimatingPI.main("EstimatingPI");
    }

    private PGraphics pg;

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        drawPolygon();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void drawPolygon() {
        int sides = sliderInt("sides");
        float r = slider("r");
        float angle = TWO_PI/sides;
        float sideLength = dist(r, 0, r*cos(angle), r*sin(angle));
        pg.beginShape();
        pg.noFill();
        pg.strokeWeight(1);
        pg.stroke(255);
        pg.translate(width*.5f, height*.5f);
        for(int i = 0; i <= sides; i++){
            pg.vertex(r*cos(angle*i), r*sin(angle*i));
        }
        pg.endShape();
        pg.fill(255);
        pg.textAlign(CENTER, CENTER);
        pg.textSize(40);
        float piEstimate = ((sideLength / r) * sides) / 2;
        pg.text("π ≈ " + piEstimate,  0, -200);
    }


    public float angularDiameter(float r, float size) {
        return atan(2 * (size / (2 * r)));
    }
}
