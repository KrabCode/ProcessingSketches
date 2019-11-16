import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-03
 */
@SuppressWarnings("SuspiciousNameCombination")
public class Orbital extends KrabApplet {
    private ArrayList<P> ps = new ArrayList<P>();
    private PGraphics pg;
    private float t;
    private PVector center;
    private PVector origin = new PVector();

    public static void main(String[] args) {
        KrabApplet.main("Orbital");
    }

    public void settings() {
//        size(800, 800, P3D);
        fullScreen(P3D, 1);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        center = new PVector(pg.width, pg.height).mult(.5f);
        recordingFrames *= 2;
    }

    public void draw() {
        t += radians(slider("t", 1));
        pg.beginDraw();
        pg.translate(center.x+slider("x offset", -200, 200), center.y+slider("y offset", -200, 200));
        alphaFade(pg);

        pg.push();
        pg.rotateX(slider("planet x rot", PI));
        pg.rotateY(slider("planet y rot", HALF_PI, PI+HALF_PI));
        pg.rotateZ(t);
        spiralSphere(pg);
        pg.pop();

        pg.rotateX(slider("planet x rot"));
        pg.rotateY(slider("planet y rot") + HALF_PI);
        pg.rotateZ(HALF_PI);
        updateParticles();
        rgbSplitPass(pg);


        pg.endDraw();
        image(pg, 0, 0);

        if(toggle("crosshairs")){
            stroke(255);
            line(0, height*.5f, width, height*.5f);
            line(width*.5f, 0, width*.5f, height);
        }

        rec(pg);
        gui();
    }

    private void spiralSphere(PGraphics pg) {
        pg.beginShape(POINTS);
        pg.stroke(255);
        pg.strokeWeight(slider("pl weight", 10));
        pg.noFill();
        float N = slider("pl count", 3000);
        float s  = 3.6f/sqrt(N);
        float dz = 2.0f/N;
        float lon = 0;
        float  z = 1 - dz/2;
        float scl = slider("pl scl", 260);
        for (int k = 0; k < N; k++) {
            float r = sqrt(1-z*z);
            pg.vertex(cos(lon)*r*scl, sin(lon)*r*scl, z*scl);
            z = z - dz;
            lon = lon + s/r;
        }
        pg.endShape();
        pg.noStroke();
        pg.fill(0);
        pg.sphereDetail(20);
        pg.sphere(slider("pl scl") - slider("planet core", 5));
    }

    private void updateParticles() {
        if (button("reset ps")) {
            ps.clear();
        }
        int intendedParticleCount = floor(slider("count", 30000));
        while (ps.size() < intendedParticleCount) {
            ps.add(new P());
        }
        while (ps.size() > intendedParticleCount) {
            ps.remove(0);
        }
        for (P p : ps) {
            p.update();
        }
    }

    class P {
        PVector pos = PVector.random3D();
        PVector prevPos = null;
        PVector spd = new PVector();

        void update() {
            spd.add(accelerate());
            spd.mult(slider("drag", .8f, 1.f));
            pos.add(spd);
            if (prevPos != null) {
                pg.strokeWeight(slider("w", 3));
                pg.stroke(255, 255 * slider("p alpha"));
                pg.line(pos.x, pos.y, pos.z, prevPos.x, prevPos.y, prevPos.z);
            } else {
                prevPos = new PVector();
            }
            prevPos.x = pos.x;
            prevPos.y = pos.y;
            prevPos.z = pos.z;
        }

        private PVector accelerate() {
            PVector acc = PVector.random3D().mult(slider("random", 5));

            PVector orbital = PVector.sub(origin, pos).normalize().mult(slider("orbital", 10));
            // orbital is the normalized vector towards the center for now
            orbital.y = orbital.z;
            orbital.z = 0;
            // X and Z in 3D space need to be converted into X and Y in top-down 2D space so I can rotate it nicely
            orbital.rotate(QUARTER_PI);
            // unfold it back to the 3D space
            orbital.z = orbital.y;

            // keep the y position near 0
            float verticalCentralizeMag = slider("vertical mag");
            orbital.y = pos.y > 0 ? -verticalCentralizeMag : verticalCentralizeMag;

            acc.add(orbital);
            return acc;
        }
    }
}






















