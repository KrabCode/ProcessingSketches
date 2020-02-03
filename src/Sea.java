import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class Sea extends KrabApplet {
    private ArrayList<Particle> particles = new ArrayList<Particle>();
    private ArrayList<Particle> particlesToRemove = new ArrayList<Particle>();
    private float borderRadius = 300;
    private PGraphics pg;
    private PVector center;

    public static void main(String[] args) {
        Sea.main("Sea");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        center = new PVector(width * .5f, height * .5f);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB,1,1,1,1);
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        group("global");
        pg.beginDraw();
        if (frameCount < 5) {
            pg.background(0);
        }
        alphaFade(pg);
        drawBorder();
        drawSea();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void drawSea() {
        group("particles");
        particles.removeAll(particlesToRemove);
        particlesToRemove.clear();
        int particleCount = sliderInt("count");
        while (particles.size() < particleCount) {
            particles.add(new Particle());
        }
        while (particles.size() > particleCount) {
            particles.remove(0);
        }
        for (Particle p : particles) {
            p.update();
        }
    }

    private void drawBorder() {
        group("border");
        borderRadius = slider("radius", 300);
        pg.noFill();
        pg.strokeWeight(slider("weight"));
        pg.stroke(picker("stroke").clr());
        pg.ellipse(center.x, center.y, borderRadius, borderRadius);
    }

    class Particle {
        PVector pos = spawn();
        int frameCreated = frameCount;
        float freqOffset = random(TWO_PI);
        float angleVariationOffset = random(TWO_PI);

        private PVector spawn() {
            float baseAngle = slider("base angle", 0);
            float theta = baseAngle+PI+random(-HALF_PI, HALF_PI);
            float radius = borderRadius*.5f+random(borderRadius*.5f);
            float horizonY = slider("horizon y", 0);
            PVector localPos = new PVector(radius * cos(theta), radius*sin(theta));
            if(localPos.y < horizonY){
                localPos.y = random(horizonY, height*.5f);
            }
            return PVector.add(center, localPos);
        }

        void update() {
            move();
            if(frameCount < 5 || isInView(pos)){
                display();
            }
            if(!isInsideBounds(pos)){
                particlesToRemove.add(this);
            }
        }

        private void display() {
            float fadeInDuration = slider("fade in duration");
            float fadeInNorm = constrain(norm(frameCount, frameCreated, frameCreated+fadeInDuration),0,1);
            HSBA stroke = picker("stroke");
            pg.stroke(stroke.hue(), stroke.sat(), stroke.br(), stroke.alpha()*fadeInNorm);
            pg.strokeWeight(slider("weight"));
            pg.point(pos.x, pos.y);
        }

        private void move() {
            float baseAngle = slider("base angle", 0);
            float speed = slider("speed", 1);
            float freq = slider("freq", .1f);
            float angleVariation = slider("angleVariation", 1)+angleVariationOffset*slider("angle var offset");
            float rotationOffset = angleVariation*cos(radians(frameCount)*(freq+freqOffset*slider("freq offset")));
            PVector spd = PVector.fromAngle(baseAngle+rotationOffset).mult(speed);
            pos.add(spd);
        }


        private boolean isInsideBounds(PVector p) {
            return PVector.dist(p, center) < borderRadius;
        }

        private boolean isInView(PVector p) {
            return PVector.dist(p, center) < borderRadius*.5f;
        }
    }
}
