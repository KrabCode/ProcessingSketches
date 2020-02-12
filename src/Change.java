import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2020-02-06
 */
public class Change extends KrabApplet {
    private PGraphics pg;
    ArrayList<Particle> ps = new ArrayList<Particle>();
    PVector center;

    public static void main(String[] args) {
        KrabApplet.main("Change");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.endDraw();
        center = new PVector(width*.5f, height*.5f);
    }

    public void draw() {
        pg.beginDraw();
        alphaFade(pg);
        updateParticles();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void updateParticles() {
        int intendedParticleCount = floor(slider("count", 1000));
        if(button("reset")){
            ps.clear();
        }
        while (ps.size() < intendedParticleCount) {
            ps.add(new Particle());
        }
        while (ps.size() > intendedParticleCount) {
            ps.remove(0);
        }
        for (Particle p : ps) {
            p.update();
        }
    }

    class Particle{
        PVector pos;
        PVector spd = new PVector();

        Particle() {
            this.pos = new PVector(width*.5f+randomGaussian()*10,height*.5f+randomGaussian()*10);
        }

        void update(){
            PVector acc = new PVector();
            acc.add(sliderXY("gravity", 0, 1, 10));
            acc.add(sliderXY("wind"));
            spd.add(acc);
            spd.mult(slider("drag", .9f));
            enforceConstraints();
            spd.limit(slider("spd limit", 10));
            pos.add(spd);
            pg.strokeWeight(slider("weight"));
            pg.stroke(1);
            pg.point(pos.x, pos.y);
        }

        private void enforceConstraints() {
            PVector toCenter = PVector.sub(center, pos);
            float mag = toCenter.mag();
            spd.add(toCenter).mult(mag*slider("to center mag"));
            pos.y = min(pos.y, height);
            for (Particle p : ps) {
                if(p.equals(this)){
                    continue;
                }
                if(dist(p.pos.x, p.pos.y, pos.x, pos.y) < slider("repulsion dist")){
                    PVector awayFromParticle = PVector.sub(pos, p.pos);
                    float awayMag = awayFromParticle.mag();
                    spd.add(awayFromParticle.normalize().mult((1/awayMag)*slider("repulsion mag")));
                }
            }
        }
    }
}
