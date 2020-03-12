import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Created by Jakub 'Krab' Rak on 2020-03-12
 */
public class ChaosGame extends KrabApplet {
    int n = 4;
    PVector p;
    PVector[] targets;
    private PGraphics pg;
    private float r = 350;

    public static void main(String[] args) {
        KrabApplet.main("ChaosGame");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        for (int i = 0; i < 2; i++) {
            pg.beginDraw();
            pg.background(0);
            pg.endDraw();
        }
        generateTargets();
        p = PVector.random2D().mult(width * .5f);
    }

    public void draw() {
        int intendedN = sliderInt("n", n);
        if (intendedN != n) {
            n = intendedN;
            generateTargets();
        }
        pg.beginDraw();
        if (button("reset")) {
            pg.background(0);
        }
        alphaFade(pg);
        pg.translate(width * .5f, height * .5f);
        PVector tran = sliderXY("translate");
        pg.translate(tran.x, tran.y);
        if (toggle("display targets")) {
            displayTargets();
        }
        for (int i = 0; i < sliderInt("speed", 1); i++) {
            chaosGame();
        }
        splitPass(pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void generateTargets() {
        targets =  new PVector[n];
        for (int i = 0; i < n; i++) {
            float theta = map(i, 0, n, 0, TAU) - HALF_PI;
            PVector target = new PVector(r * cos(theta), r * sin(theta));
            targets[i] = target;
        }
    }

    private void displayTargets() {
        pg.stroke(255, 0, 0);
        pg.strokeWeight(3);
        for (int i = 0; i < n; i++) {
            PVector target = targets[i];
            pg.point(target.x, target.y);
        }
    }

    private void chaosGame() {
        int randomIndex = floor(random(n));
        moveTo(targets[randomIndex]);
        pg.stroke(picker("stroke").clr());
        pg.strokeWeight(slider("weight", 1));
        pg.point(p.x, p.y);
    }

    private void moveTo(PVector target) {
        p = PVector.lerp(p, target, .5f);
    }
}
