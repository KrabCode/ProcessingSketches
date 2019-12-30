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
    private PGraphics snowPg;

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
        frameRecordingDuration *= 4;
    }

    private void resetPGraphics() {
        pg = createGraphics(width / scaledown, height / scaledown, P2D);
        snowPg = createGraphics(width*2, height*2, P2D);

    }

    public void draw() {
        int intendedScaledown = sliderInt("scaledown", 1);
        if (intendedScaledown > 0 && intendedScaledown != scaledown) {
            scaledown = intendedScaledown;
            resetPGraphics();
        }
        pg.beginDraw();
        rayMarchPass(pg);
        pg.endDraw();
        image(pg, 0, 0, width, height);
        /*
        if(toggle("snow", false)){
            snowPg.beginDraw();
            snowPg.clear();
            updateParticles();
            snowPg.endDraw();
            translate(width/2f, height/2f);
            int mirrors = sliderInt("mirrors");
            for(int i = 0; i < mirrors; i++){
                float a = map(i, 0, mirrors-1, 0, TAU);
                pushMatrix();
                rotate(a);
                image(snowPg, -width, -height,width, height);
                popMatrix();
            }
        }
        */
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
                    random(-snowPg.width*2,snowPg.width),
                    -50-random(snowPg.height),
                    random(snowPg.width)
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
            if(pos.y > snowPg.height*2 || pos.x > snowPg.width*2 || pos.x < -snowPg.width*2){
                psBin.add(this);
            }
            float zDarken = norm(pos.z, 0, width);
            float w = slider("weight");
            snowPg.strokeWeight(constrain(w-zDarken, 1, 10));
            snowPg.stroke(255-zDarken*slider("darken"));
            snowPg.point(pos.x, pos.y, pos.z);
        }
    }


}
