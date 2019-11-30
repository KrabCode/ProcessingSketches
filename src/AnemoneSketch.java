import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-26
 */
public class AnemoneSketch extends KrabApplet {
    private PGraphics pg;
    private Anemone anemone;

    public static void main(String[] args) {
        KrabApplet.main("AnemoneSketch");
    }

    public void settings() {
        size(800, 800, P3D);
//        fullScreen(P3D, 1);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        anemone = new Anemone();
    }

    public void draw() {
        group("main");
        pg.beginDraw();
        alphaFade(pg);
        splitPass(pg);
        PVector pos = sliderXYZ("translate");
        PVector rot = sliderXYZ("rotate");
        pg.translate(width * .5f + pos.x, height * .5f + pos.y, pos.z);
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z);
        anemone.update();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    class Anemone {
        PVector center = new PVector();
        int tentacleCount = 0;
        private ArrayList<Tentacle> tentacles = new ArrayList<Tentacle>();
        private ArrayList<Tentacle> tentaclesToRemove = new ArrayList<Tentacle>();

        void update() {
            updateTentacles();
            pg.stroke(.7f);
            pg.strokeWeight(1);
            pg.point(center.x, center.y, center.z);
        }

        private void updateTentacles() {
            group("tentacles");
            int intendedTentacleCount = sliderInt("count");
            if (intendedTentacleCount != tentacleCount) {
                tentacleCount = intendedTentacleCount;
                regenTentacles();
            }
            for (Tentacle t : tentacles) {
                t.update();
                t.display();
            }
            tentacles.removeAll(tentaclesToRemove);
            tentaclesToRemove.clear();
        }

        private void regenTentacles() {
            tentacles.clear();
            int spawnCount = sliderInt("spawn count");
            ArrayList<PVector> roots = spiralSphere(spawnCount, slider("scl"));
            for (int i = 0; i < min(tentacleCount, spawnCount); i++) {
                tentacles.add(new Tentacle(roots.get(i)));
            }
        }

        class Tentacle {
            ArrayList<PVector> joints = new ArrayList<PVector>();

            public Tentacle(PVector root) {
                joints.add(root);
            }

            void update() {
                for (PVector joint : joints) {
                    pg.stroke(1);
                    pg.strokeWeight(1);
                    pg.point(joint.x, joint.y, joint.z);
                }
            }

            void display() {

            }
        }

    }
}
