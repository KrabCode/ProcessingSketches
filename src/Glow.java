import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class Glow extends HotswapGuiSketch {
    float worldRadius;
    ArrayList<P> ps = new ArrayList<P>();
    float t;
    int intendedParticleCount;
    PGraphics pg;

    public static void main(String[] args) {
        HotswapGuiSketch.main("Glow");
    }

    public void settings() {
//        size(800, 800, P3D);
        fullScreen(P3D);
    }

    public void setup() {
        pg = createGraphics(width,height,P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        t += radians(1);
        pg.beginDraw();
        transparentWhitePass(g);
        pg.translate(pg.width*.5f,pg.height*.5f);
        updateParticles();
        noiseOffsetPass(t, pg);
        rgbSplitUniformPass(pg);
        noisePass(t, pg);
        pg.endDraw();
        rec(pg);
        image(pg, 0, 0, width, height);
        gui();
    }



    private void updateParticles() {
        worldRadius = slider("world radius", 2000);
        intendedParticleCount = floor(slider("count", 2000));
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
        PVector pos;
        String axis = randomAxis();
        int dirSign = random(1) > .5f ? -1 : 1;
        float sizeMult = 1.f+randomGaussian();
        float spdMult = 1.f+randomGaussian();
        int fadeInStarted = 0;
        int fadeInDuration = 60;

        P() {
            float x = random(-worldRadius, worldRadius);
            float y = random(-worldRadius, worldRadius);
            float z = random(-worldRadius, worldRadius);
            pos = new PVector(x, y, z);
        }

        private String randomAxis() {
            float n = random(1);
            if (n < .33) {
                return "x";
            } else if (n < .66) {
                return "y";
            }
            return "z";
        }

        void update() {
            move();
            checkLeaveBounds();
            draw();
        }

        void draw(){
            pg.stroke(255);
            pg.noFill();
            pg.pushMatrix();
            pg.pushStyle();
            pg.translate(pos.x, pos.y, pos.z);
            float fadeInNormalized = constrain(norm(frameCount, fadeInStarted, fadeInStarted+fadeInDuration), 0,1);
            pg.stroke(255,fadeInNormalized*255);
            pg.box(slider("size", 20)*sizeMult);
            pg.popStyle();
            pg.popMatrix();
        }

        private void move() {
            float spd = slider("speed", 10)*spdMult;
            if (axis.equals("x")) {
                pos.x += spd * dirSign;
            } else if (axis.equals("y")) {
                pos.y += spd * dirSign;
            } else if (axis.equals("z")) {
                pos.z += spd * dirSign;
            }
        }

        private void checkLeaveBounds() {
            float x = pos.x;
            float y = pos.y;
            float z = pos.z;
            if(pos.x < -worldRadius){
                pos.x += worldRadius*2;
            }
            if(pos.x > worldRadius){
                pos.x -= worldRadius*2;
            }
            if(pos.y < -worldRadius){
                pos.y += worldRadius*2;
            }
            if(pos.y > worldRadius){
                pos.y -= worldRadius*2;
            }
            if(pos.z < -worldRadius){
                pos.z += worldRadius*2;
            }
            if(pos.z > worldRadius){
                pos.z -= worldRadius*2;
            }
            if(pos.x != x || pos.y != y || pos.z != z){
                fadeInStarted = frameCount;
            }
        }
    }
}
