import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class RecursiveDrawing extends KrabApplet {
    private PVector center;
    PVector a = new PVector(), b = new PVector();

    public static void main(String[] args) {
        RecursiveDrawing.main("RecursiveDrawing");
    }

    private PGraphics pg;

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB,1,1,1,1);
        pg.background(0);
        pg.endDraw();
        center = new PVector(width, height).mult(.5f);
    }

    public void draw() {
        pg.beginDraw();
        alphaFade(pg);
        splitPass(pg);
        pg.translate(center.x, center.y);
        pg.stroke(picker("stroke", 1));
        addHue("stroke", radians(1)/TWO_PI);
        pg.strokeWeight(slider("weight", 1));
        int mirrors = sliderInt("mirrors", 0, 100, 10);
        int distanceCopies = sliderInt("dist copies", 0, 100, 3);
//        PVector mouse = new PVector(mouseX, mouseY).sub(center);
//        PVector pmouse = new PVector(pmouseX, pmouseY).sub(center);
        b = a.copy();
        a = sliderXY("a").copy();
        a.setMag(a.mag()*(1+.5f*sin(t)));
        a.x *= cos(a.heading() + t);
        a.y *= sin(a.heading() + t);

        for (int i = 0; i <= mirrors; i++) {
            float iNorm = norm(i, 0, mirrors);
            pg.pushMatrix();
            pg.rotate(iNorm * TWO_PI);
//            boolean pointOnly = dist(mouseX, mouseY, pmouseX, pmouseY) < 1;
            for (int j = 0; j < distanceCopies; j++) {
                float jNorm = norm(j, 0, distanceCopies-1);
//                if(pointOnly){
//                    pg.point(a.x*jNorm, a.y*jNorm);
//                }else{
                    pg.line(a.x*jNorm, a.y*jNorm, b.x*jNorm, b.y*jNorm);

            }
            pg.popMatrix();
        }
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
