package unfinished;

import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class Lissajous3D extends HotswapGuiSketch {
    float t = 0;
    PGraphics pg;
    ArrayList<P> ps = new ArrayList<>();
    private float lMax;

    public static void main(String[] args) {
        HotswapGuiSketch.main("unfinished.Lissajous3D");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
    }

    public void draw() {
        t += radians(slider("t", 0,1,1));
        lMax = slider("lmax", 10);
        pg.beginDraw();
        alphaFade(pg);
        pg.translate(width*.5f,height*.5f);
        updateParticles();
        rgbSplitUniformPass(pg);
//        noiseOffsetPass(t, pg);
//        noisePass(t, pg);

        pg.endDraw();
        rec(pg);
        image(pg, 0,0,width,height);
        gui();
    }

    private void updateParticles() {
        int intendedParticleCount = floor(slider("count", 500));
        while(ps.size() < intendedParticleCount){
            ps.add(new P());
        }
        while(ps.size() > intendedParticleCount){
            ps.remove(0);
        }
        for(P p : ps){
            p.update();
        }
    }

    class P {
        PVector pos;
        PVector lastPos;

        float a = random(-lMax, lMax);
        float b = random(-lMax, lMax);

        P(){
            pos = new PVector();
            lastPos = new PVector();
        }

        void update() {
            float r = slider("r", 400);
            pos.x = r*cos(t*a);
            pos.y = r*sin(t*b);

            pg.strokeWeight(3);
            pg.stroke(255);
            pg.noFill();
            if(lastPos.mag() > 0){
                pg.line(lastPos.x, lastPos.y, lastPos.z, pos.x, pos.y, pos.z);
            }

            lastPos.x = pos.x;
            lastPos.y = pos.y;
            lastPos.z = pos.z;
        }
    }
}
