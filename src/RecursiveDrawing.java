import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class RecursiveDrawing extends KrabApplet {
    private PVector center;
    private PGraphics pg;
    private ArrayList<Particle> particles = new ArrayList<Particle>();
    public static void main(String[] args) {
        RecursiveDrawing.main("RecursiveDrawing");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.background(0);
        pg.endDraw();
        center = new PVector(width, height).mult(.5f);

    }

    public void draw() {
        pg.beginDraw();
        alphaFade(pg);
//        splitPass(pg);
        pg.translate(center.x, center.y);
        pg.strokeWeight(slider("weight", 1));
        int mirrors = sliderInt("mirrors", 0, 100, 1);
        group("particle");
        PVector color = pickerVector("stroke");
        addHue("stroke", slider("hue delta"));
        updateParticles();
        for (int i = 0; i <= mirrors; i++) {
            float iNorm = norm(i, 0, mirrors);
            pg.pushMatrix();
            pg.rotate(iNorm * TWO_PI);
            for(Particle p : particles){
                pg.stroke((color.x + p.hueOffset*slider("hue offset range")) % 1, color.y, color.z);
                pg.line(p.pos.x-center.x, p.pos.y-center.y, p.prev.x-center.x, p.prev.y-center.y);
            }
            pg.popMatrix();
        }
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void updateParticles() {
        int count = sliderInt("count", 20);
        while(particles.size() < count){
            particles.add(new Particle());
        }
        while(particles.size() > count){
            particles.remove(0);
        }
        for(Particle p : particles){
            p.update();
        }
    }

    class Particle{
        PVector pos = PVector.random2D().mult(width), spd = new PVector();
        PVector prev = pos.copy();
        float hueOffset = randomGaussian();
        float dragOffset = 1+random(.2f);

        void update(){
            PVector toCenter = PVector.sub(center, pos);
            spd.add(toCenter.copy().rotate(HALF_PI).normalize().mult(slider("orbit")));
            spd.add(toCenter.normalize().mult(slider("to center")));
            spd.add(new PVector(randomGaussian(), randomGaussian()).mult(slider("gauss")));
            spd.mult(slider("drag")*dragOffset);
            prev = pos.copy();
            pos.add(spd);
        }
    }
}
