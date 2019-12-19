import applet.KrabApplet;
import processing.core.PVector;

public class DotProduct extends KrabApplet {

    PVector origin = new PVector();
    int black = color(0);
    int white = color(255);

    public static void main(String[] args) {
        KrabApplet.main("DotProduct");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {

    }

    public void draw() {
        background(0);
        translate(width * .5f, height * .5f);
        PVector red = sliderXYZ("red").copy();
        PVector blue = sliderXYZ("blue").copy();
        noFill();
        strokeWeight(3);
        depthLine(origin, red, color(255, 0, 0));
        depthLine(origin, blue, color(0, 0, 255));
        float dotProduct = PVector.dot(red.normalize(), blue.normalize());
        translate(0, -200);
        fill(255);
        textSize(30);
        textAlign(CENTER, CENTER);
        text(dotProduct, 0, 0);
        gui();
    }

    private void depthLine(PVector origin, PVector target, int clr) {
        beginShape();
        int count = 100;
        for (int i = 0; i < count; i++) {
            float amt = norm(i, 0, count - 1);
            float x = lerp(origin.x, target.x, amt);
            float y = lerp(origin.y, target.y, amt);
            float z = lerp(origin.z, target.z, amt);
            float zNorm = constrain(norm(z, 300, -1000), 0, 1);
            int colorToDarken = clr;
            if(i % 10 == 0){
                colorToDarken = white;
            }
            int darkenedWithDepth = lerpColor(colorToDarken, black, zNorm);
            stroke(darkenedWithDepth);
            vertex(x, y, z);
        }
        endShape();
    }
}
