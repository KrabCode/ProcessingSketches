package boids;

import processing.core.PGraphics;
import processing.core.PVector;

public class Particle {
    public float scl;
    private Boids p;
    private float h;
    private float w;
    private int clr;
    protected PVector pos = new PVector();
    protected PVector spd = new PVector();
    protected PVector acc = new PVector();
    private PGraphics pg;

    Particle(Boids p, int clr, float scl, PGraphics pg) {
        this.pg = pg;
        this.p = p;
        this.clr = clr;
        this.scl = scl;
        w = p.random(10);
        h = p.random(10);
    }

    public void update() {
        spd.add(acc);
        spd.mult(.9f);
        pos.add(spd);
        acc.mult(0);
    }

    public void display() {
        pg.fill(clr);
        pg.noStroke();
        pg.pushMatrix();
        pg.translate(pos.x, pos.y);
        pg.scale(scl);
        pg.ellipse(0,0,w, h);
        pg.popMatrix();
        w -= .1f;
        h -= .1f;
    }

    public boolean isDead() {
        return w < 0 || h < 0;
    }
}
