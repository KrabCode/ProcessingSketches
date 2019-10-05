package boids;

import processing.core.PGraphics;
import processing.core.PVector;

public class ParticleBlood implements IParticle {
    private Boids p;
    private PGraphics pg;
    private float h;
    private float w;
    float shade;
    protected PVector pos = new PVector();
    protected PVector spd = new PVector();
    protected PVector acc = new PVector();

    ParticleBlood(Boids p, PGraphics pg) {
        this.p = p;
        this.pg = pg;
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
        pg.fill(255-shade,0,0);
        pg.noStroke();
        pg.ellipse(pos.x,pos.y,w, h);
        w -= .1f;
        h -= .1f;
    }

    public boolean isDead() {
        return w < 0 || h < 0;
    }
}
