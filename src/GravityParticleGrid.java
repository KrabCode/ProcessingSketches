import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-16
 */
public class GravityParticleGrid extends KrabApplet {
    int count = 40;
    private PVector origin = new PVector();
    private PGraphics pg;
    private ArrayList<P> ps = new ArrayList<>();

    public static void main(String[] args) {
        KrabApplet.main("GravityParticleGrid");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        recordingFrames *= 2;
    }

    // point rain falling on a grid of points which ripple with the 2D water effect
    public void draw() {
        pg.beginDraw();
        group("translate");
        PVector translate = new PVector(width, height).mult(.5f);
        pg.translate(translate.x, translate.y, slider("translate z", 0, 100));
        updateParticles();
        updateMap();
        group("shaders");
        alphaFade(pg);
        rgbSplitPass(pg);
        noisePass(t, pg);
        pg.endDraw();
        background(0);
        image(pg, 0, 0);
        rec(pg);
        gui();
    }


    private void updateMap() {
        group("grid");
        count = floor(slider("count", 10, 100));
        for (int x = 0; x < count; x++) {
            for (int y = 0; y < count; y++) {
                for (int z = 0; z < count; z++) {
                    updateCell(x, y, z);
                    drawCell(x, y, z);
                }
            }
        }
    }

    private void drawCell(int x, int y, int z) {
        float spread = slider("spread", 100);
        float screenX = map(x, 0, count - 1, -spread, spread);
        float screenY = map(y, 0, count - 1, -spread, spread);
        float screenZ = map(z, 0, count - 1, -spread, spread);
        pg.stroke(picker("stroke", 1));
        pg.strokeWeight(slider("weight", 1));
        pg.point(screenX, screenY, screenZ);
    }

    private void updateCell(int x, int y, int z) {

    }

    private void updateParticles() {
        group("particles");
        int intendedParticleCount = floor(slider("count", 0, 1000));
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
        PVector pos = PVector.random3D().mult(slider("spawn", 300));
        PVector spd = new PVector();

        void update() {
            spd.add(PVector.random3D().mult(randomGaussian() * slider("random gaussian")));
            PVector towardsCenter = PVector.sub(origin, pos);
            spd.add(towardsCenter.normalize().mult(1 / towardsCenter.mag()).mult(slider("gravity")));
            group("noise");
            noiseDetail(floor(slider("detail", 0, 100, 5)));
            float freq = slider("frequency");
            float n = slider("magnitude", 0) * ( 1 - 2 * noise(pos.x*freq, pos.y*freq, pos.z*freq));

            group("particles");
            PVector orbital = PVector.sub(origin, pos).normalize().mult(slider("orbital", 10));
            // orbital is the normalized vector towards the center for now
            orbital.y = orbital.z;
            orbital.z = 0;
            // X and Z in 3D space need to be converted into X and Y in top-down 2D space so I can rotate it nicely
            orbital.rotate(QUARTER_PI+n);
            // unfold it back to the 3D space
            orbital.z = orbital.y;
            // keep the y position near 0
            float verticalCentralizeMag = slider("vertical mag");
            orbital.y = pos.y > 0 ? -verticalCentralizeMag : verticalCentralizeMag;


            spd.add(orbital.normalize().mult(slider("orbital")));
            spd.mult(slider("drag", 1));
            PVector prev = new PVector().add(pos);
            pos.add(spd);
            pg.stroke(picker("stroke", 1));
            pg.strokeWeight(slider("weight", 1));
            pg.line(pos.x, pos.y, pos.z, prev.x, prev.y, prev.z);

        }

    }
}
