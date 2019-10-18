import applet.HotswapGuiSketch;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2019-10-18
 */
public class TriangleStripLine extends HotswapGuiSketch {
    private PGraphics pg;
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("TriangleStripLine");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        pg.beginDraw();
        pg.background(0);
        pg.noFill();
        pg.stroke(255);
        pg.translate(width*.5f, height*.5f);
        pg.rotateX(slider("rotx"));
        pg.rotateY(slider("roty"));
        pg.rotateZ(slider("rotz"));

        float r = 30;
        int faceCount = 3;
        for(float theta = 0; theta < TWO_PI; theta+=TWO_PI/faceCount){
            pg.beginShape(TRIANGLE_STRIP);
            float thetaNext = theta+TWO_PI/faceCount;
            for(int z = 1000; z > -1000; z -= 10){
                pg.vertex(r*cos(theta), r*sin(theta), z);
                pg.vertex(r*cos(thetaNext), r*sin(thetaNext), z);
            }
            pg.endShape();
        }

        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
