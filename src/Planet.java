import applet.HotswapGuiSketch;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;

public class Planet extends HotswapGuiSketch {
    private float t;

    PGraphics pg;
    PShape sphere;

    public static void main(String[] args) {
        HotswapGuiSketch.main("Planet");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(500,500,P2D);
        sphere = createSphere(250, 20);
    }

    public void draw() {
        t += radians(slider("time"));

        pg.beginDraw();
        pg.background(0);
        pg.noStroke();
        pg.fill(0);
        pg.line(0,0,width,height);
        pg.endDraw();

        background(100);
        translate(width*.5f, height*.5f);
        rotateY(t);
        texture(pg);
        shape(sphere);

        gui();
    }

    PShape createSphere(float r, float step){
        PShape sphere = createShape();
        sphere.beginShape(PConstants.TRIANGLE_STRIP);
        sphere.textureMode(NORMAL);
        sphere.noStroke();
        sphere.fill(0);
        float rowAngularDiameter = angularDiameter(r, step);
        int rowCount = floor(PI/rowAngularDiameter);
        for (int i = 0; i < rowCount; i++) {
            //go from north pole to south pole
            float rowAngle = map(i, 0, rowCount-1, -HALF_PI, HALF_PI);
            float rowRadius = r*cos(rowAngle);
            float columnAngularStep = angularDiameter(rowRadius, step);
            int columnCount = max(1, floor(TWO_PI/columnAngularStep)); //at least 1 column should be shown at poles
            for (int j = 0; j < columnCount; j++) {
                //go all the way around the sphere
                float columnAngle = map(j, 0, columnCount, 0, TWO_PI);
                pushMatrix();
                rotateY(columnAngle);
                rotateZ(rowAngle);
                float rowNorm = norm(rowAngle, -HALF_PI, HALF_PI);
                float colNorm = norm(columnAngle, 0, TWO_PI);
//                float elevation = .2f*r*noise(5+5*cos(columnAngle), 5+5*cos(rowAngle*2), t);
                translate(r, 0, 0);
                rotateY(HALF_PI);
                rectMode(CENTER);
                sphere.vertex(modelX(0,0,0), modelY(0,0,0), modelZ(0,0,0), rowNorm, colNorm);
                //TODO triangle strip with the next row
                popMatrix();
            }
        }
        sphere.endShape();
        return sphere;
    }

}
