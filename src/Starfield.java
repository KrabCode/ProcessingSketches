import applet.CustomPApplet;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;


public class Starfield extends CustomPApplet {
    ArrayList<P> ps = new ArrayList<P>();
    ArrayList<P> psBin = new ArrayList<P>();
    int startBuffer = 2000;
    float birthDistanceFromCamera = 2000;
    float speed = 10;
    float time = startBuffer;

    public static void main(String[] args) {
        PApplet.main("Starfield");
    }

    public void settings() {
        size(600, 600, P3D);
    }

    public void setup() {
        background(0);
        for (int i = 0; i < startBuffer; i += speed) {
            ps.add(new P(i));
        }
        colorMode(HSB, 255, 255, 255, 100);
    }

    public void draw() {
        super.draw();
        time += speed;
        pushMatrix();
        animate(time);
        popMatrix();
        speed = slider("speed", 0, 10, 2);
    }

    void animate(float t) {
        background(0);
        translate(width * .5f, height * .5f, t);
        for (int i = 0; i < speed; i += 10) {
            ps.add(new P(t));
        }
        if (ps.size() > 2000) {
            ps.remove(0);
        }
        updatePs(t);
        if(t < startBuffer+300*speed){
            saveFrame(captureDir+"####.jpg");
        }
    }

    void updatePs(float t) {
        for (P p : ps) {
            p.update(t);
            if (p.dead) {
                psBin.add(p);
            }
        }
        ps.removeAll(psBin);
        psBin.clear();
        // println(ps.size() + " @ " + frameRate);
    }

    class P extends PVector {
        boolean dead = false;
        int born = frameCount;
        int fadeIn = 30;
        float weight = 2 + randomGaussian();

        P(float t) {
            super(
                    randomGaussian() * width,
                    randomGaussian() * height,
                    -t - birthDistanceFromCamera
            );
        }

        void update(float t) {
            float fadeInNormalized = map(frameCount, born, born + fadeIn, 0, 1);
            stroke(255, 255 * fadeInNormalized);
            strokeWeight(weight);
            point(x, y, z);
            if (z - 1000 > -t) {
                dead = true;
            }
        }
    }

}
