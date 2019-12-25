import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2019-12-07
 */
public class Raymarch extends KrabApplet {
    private int scaledown = 8;
    private ArrayList<P> ps = new ArrayList<P>();
    private ArrayList<P> psBin = new ArrayList<P>();;
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("Raymarch");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        surface.setLocation(1920 - 820, 20);
        resetPGraphics();
        frameRecordingDuration *= 2.1;
    }

    private void resetPGraphics() {
        pg = createGraphics(width / scaledown, height / scaledown, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        int intendedScaledown = sliderInt("scaledown", 1);
        if (intendedScaledown > 0 && intendedScaledown != scaledown) {
            scaledown = intendedScaledown;
            resetPGraphics();
        }
        pg.beginDraw();
        rayMarchPass(pg);
        updateParticles();
        pg.endDraw();
        image(pg, 0, 0, width, height);
        rec(pg);
        gui();
    }

    private void updateParticles() {
        int intendedCount = sliderInt("count");
        while (ps.size() < intendedCount) {
            ps.add(new P());
        }
        while (ps.size() > intendedCount) {
            ps.remove(0);
        }
        ps.removeAll(psBin);
        psBin.clear();
        for (P p : ps) {
            p.update();
        }
    }

    class P {
        PVector pos, spd;

        P() {
            pos = new PVector(
                    random(-width*2,width),
                    -50-random(height),
                    random(width)
            );
            spd = new PVector();
        }

        void update() {
            PVector acc = sliderXYZ("snow acc").copy();
            float noiseFreq = slider("noise freq");
            PVector n = PVector.fromAngle(slider("base angle") + slider("range")*noise(pos.x*noiseFreq, pos.y*noiseFreq, pos.z*noiseFreq));
            n.mult(slider("noise mag"));
            PVector g = new PVector(randomGaussian(), randomGaussian(), randomGaussian());
            g.mult(slider("gauss mag"));
            acc.add(n);
            acc.add(g);
            spd.add(acc);
            pos.add(spd);
            spd.mult(slider("drag"));
            if(pos.y > height || pos.x > width*2 || pos.x < -width){
                psBin.add(this);
            }
            float zDarken = norm(pos.z, 0, width);
            float w = slider("weight");
            pg.strokeWeight(constrain(w-zDarken, 1, 10));
            pg.stroke(255-zDarken*slider("darken"));
            pg.point(pos.x, pos.y, pos.z);
        }
    }


}
