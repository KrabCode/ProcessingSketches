import applet.KrabApplet;

/**
 * Created by Jakub 'Krab' Rak on 2020-03-12
 */
public class AngularDiameter extends KrabApplet {

    public static void main(String[] args) {
        KrabApplet.main("AngularDiameter");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
    }

    public void draw() {
        translate(width/2, height/2);
        float r = 200;
        float size = 10;
        float ad = angularDiameter(r, size);
        int shapeCount = floor(TAU / ad);
        for (int i = 0; i < shapeCount; i++) {
            float theta = map(i, 0, shapeCount, 0, TAU);
            ellipse(r*cos(theta), r*sin(theta), size, size);
        }
    }
}
