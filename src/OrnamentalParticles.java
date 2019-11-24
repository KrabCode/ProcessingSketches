import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-23
 */
public class OrnamentalParticles extends KrabApplet {

    //TODO clusters making shapes out of individual particles, leaves, geometry

    PGraphics pg;
    ArrayList<Particle> particles = new ArrayList<Particle>();
    ArrayList<Particle> particlesToRemove = new ArrayList<>();

    public static void main(String[] args) {
        KrabApplet.main("OrnamentalParticles");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        group("main");
        alphaFade(pg);
        PVector translate = sliderXYZ("translate");
        pg.translate(width * .5f + translate.x, height * .5f + translate.y, translate.z);
        updateParticles();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void updateParticles() {
        group("particles");
        int count = sliderInt("count");
        if(button("reset")){
            particles.clear();
        }
        while (particles.size() < count) {
            particles.add(new Particle());
        }
        while (particles.size() > count) {
            particles.remove(0);
        }
        for (Particle p : particles) {
            p.update();
        }
        particles.removeAll(particlesToRemove);
        particlesToRemove.clear();
    }


    private PVector randomSpawn() {
        return PVector.random3D().mult(slider("spawn distance"));
    }

    class Particle {
        PVector pos = randomSpawn(), prev = pos.copy(), spd = new PVector();
        float mass = random(slider("max mass"));

        void update() {
            group("forces");
            spd.add(gravityToOtherParticles());
            spd.limit(slider("max force"));
            spd.mult(slider("drag"));
            group("particles");
            spd.y *= slider("y mult");
            pos.add(spd);
            pg.noFill();
            pg.strokeWeight(slider("weight"));
            pg.fill(picker("stroke").clr());
            pg.noStroke();
            pg.pushMatrix();
            pg.translate(pos.x, pos.y, pos.z);
            pg.ellipse(0,0,mass, mass);
            pg.popMatrix();
            prev = pos.copy();
        }

        private PVector gravityToOtherParticles() {
            PVector acc = new PVector();
            float g = slider("gravity");
            float fromThreshold = mass*slider("from threshold");
            float fromMag = slider("from mag");
            for (Particle p : particles) {
                if (p.equals(this)) {
                    continue;
                }
                float d = dist(pos.x, pos.y, pos.z, p.pos.x, p.pos.y, p.pos.z);
                if (d == 0) {
                    continue;
                }
                if (d < fromThreshold) {
                    acc.add(PVector.sub(pos, p.pos).normalize().mult(fromMag));
                    continue;
                }
                float force = g * ((mass * p.mass) / pow(d, 2));
                PVector towardsOther = PVector.sub(p.pos, pos);
                towardsOther.setMag(force);
                acc.add(towardsOther);
            }
            return acc;
        }
    }

}
