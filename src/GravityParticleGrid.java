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
//        size(800, 800, P3D);
        fullScreen(P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        frameRecordingDuration *= 2;
    }

    // point rain falling on a grid of points which ripple with the 2D water effect
    public void draw() {
        pg.beginDraw();
        group("camera");
        alphaFade(pg);
        PVector xyz = sliderXYZ("position", width/2, height/2, 0, 1000);
        xyz.add(sliderXYZ("speed"));
        pg.translate(xyz.x, xyz.y, xyz.z);
        if(toggle("rotate")){
            PVector rot = sliderXYZ("rotation");
            pg.rotateX(rot.x);
            pg.rotateY(rot.y);
            pg.rotateZ(rot.z);
        }

        pg.push();
        group("planet");
        PVector rotate = sliderXY("rotate");
        pg.rotateX(rotate.x);
        pg.rotateY(rotate.y);
        pg.rotateZ(t);
        spiralSphere(pg);
        pg.pop();

        updateParticles();
//        updateMap();
        group("shaders");
//        chromaticAberrationPass(pg);
        rgbSplitPassUniform(pg);
        pg.endDraw();
        background(0);
        image(pg, 0, 0);
        rec(g);
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
        pg.stroke(picker("stroke", 1).clr());
        pg.strokeWeight(slider("weight", 1));
        pg.point(screenX, screenY, screenZ);
    }

    private void updateCell(int x, int y, int z) {

    }

    private void updateParticles() {
        group("particle");
        int intendedParticleCount = floor(slider("count", 1000, 1000));
        if(button("clear")){
            ps.clear();
        }
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
            group("gauss");
            PVector randomRange = sliderXYZ("xyz", 1, 10);
            spd.add(new PVector(randomGaussian()*randomRange.x,
                    randomGaussian()*randomRange.y,
                    randomGaussian()*randomRange.z).mult(slider("mag")));
            PVector towardsCenter = PVector.sub(origin, pos);
            group("particle");
            spd.add(towardsCenter.normalize().mult(1 / towardsCenter.mag()).mult(slider("gravity")));
            PVector orbital = PVector.sub(origin, pos).normalize().mult(slider("orbital", 0, 10));
            // orbital is the normalized vector towards the center for now
            orbital.y = orbital.z;
            orbital.z = 0;
            // X and Z in 3D space need to be converted into X and Y in top-down 2D space so I can rotate it nicely
            orbital.rotate(QUARTER_PI);
            // unfold it back to the 3D space
            orbital.z = orbital.y;
            orbital.normalize().mult(slider("orbital"));
            // keep the y position near 0
            float verticalCentralizeMag = slider("vertical mag");
            orbital.y = pos.y > 0 ? -verticalCentralizeMag : verticalCentralizeMag;


            spd.add(orbital);
            spd.mult(slider("drag", 1));
            pos.add(spd);
            pg.stroke(picker("stroke", 1).clr());
            pg.fill(picker("fill", 0,0).clr());
            pg.pushMatrix();
            pg.translate(pos.x, pos.y, pos.z);
            pg.rotateX(spd.x);
            pg.rotateY(spd.y);
            pg.rotateZ(spd.z);
            pg.box(slider("weight", 10));
            pg.popMatrix();
        }

    }

}
