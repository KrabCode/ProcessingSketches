import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-26
 */
public class Anemone2 extends KrabApplet {
    private PGraphics pg;
    private ArrayList<Tentacle> tentacles = new ArrayList<Tentacle>();
    private ArrayList<Tentacle> tentaclesToRemove = new ArrayList<Tentacle>();

    public static void main(String[] args) {
        KrabApplet.main("Anemone2");
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
    }

    public void draw() {
        group("main");
        pg.beginDraw();
        alphaFade(pg);
        splitPass(pg);
        PVector pos = sliderXYZ("translate");
        PVector rot = sliderXYZ("rotate");
        pg.translate(pos.x, pos.y, pos.z);
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z);
        updateTentacles();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }


    private void updateTentacles() {
        group("tentacles");
        int tentacleCount = sliderInt("count");

        for (Tentacle t : tentacles) {
            t.update();
            t.display();
            if (t.isDead) {
                tentaclesToRemove.add(t);
            }
        }
        tentacles.removeAll(tentaclesToRemove);
        tentaclesToRemove.clear();
    }


    class Tentacle{
        ArrayList<PVector> joints = new ArrayList<PVector>();

        boolean isDead;

        void update(){
            
        }

        void display() {

        }

    }
}
