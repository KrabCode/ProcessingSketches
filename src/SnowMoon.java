import applet.HotswapGuiSketch;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

import java.util.ArrayList;

public class SnowMoon extends HotswapGuiSketch {


    public static void main(String[] args) {
        HotswapGuiSketch.main("SnowMoon");
    }


    private PShape star;
    float t;
    PGraphics pg;
    float r = 300;
    float particleRadius = 2;
    private ArrayList<Particle> particles = new ArrayList<Particle>();

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        star = createShape();
        star.beginShape(TRIANGLE_STRIP);
        star.fill(255);
        star.noStroke();
        int count = 14;
        for(int i = 0; i < count; i++){
            float inorm = norm(i, 0, count-1);
            float theta = inorm*TWO_PI;
            if(i % 2 == 0){
                star.vertex(cos(theta), sin(theta));
            }else{
                star.vertex(.5f*cos(theta), .5f*sin(theta));
            }
            star.vertex(0,0);
        }
        star.endShape(CLOSE);
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        r = slider("r", 170);
        particleRadius = slider("particle radius", 5);

        pg.beginDraw();

        alphaFade(pg);

        pg.noLights();
        pg.translate(width*.5f, height*.5f);

        pg.hint(PConstants.DISABLE_DEPTH_TEST);
        float moonR = slider("moon r", 200);
        float moonInnerR = slider("moon inner r", 200);
        pg.fill(255);
        pg.noStroke();
        pg.ellipseMode(CENTER);
        pg.ellipse(0,slider("moon y", -100, 100),moonR, moonR);
        pg.fill(0);
        pg.ellipse(slider("moon x", -100, 100), slider("inner moon y", -200,200), moonInnerR, moonInnerR);


        pg.rotateY(-t);

        float intendedCount = slider("particleCount", 500);
        while(particles.size() < intendedCount){
            particles.add(new Particle());
        }
        while(particles.size() > intendedCount){
            particles.remove(particles.size()-1);
        }
        for(Particle p : particles){
            p.update();
        }

        rgbSplitPass(pg);
        noisePass(t, pg);
        noiseOffsetPass(pg, t);

        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    class Particle{
        PVector pos;
        PVector rot;
        Particle(){
            pos = new PVector(randomGaussian(), randomGaussian(), randomGaussian());
            rot = new PVector(random(TWO_PI), random(TWO_PI), random(TWO_PI));
        }

        void update(){
            pg.push();
            pg.noStroke();
            pg.translate(pos.x*r, pos.y*r, pos.z*r);
            pg.rotateX(rot.x);
            pg.rotateY(rot.y);
            pg.rotateZ(rot.z);
            pg.scale(slider("scl", 10));
            pg.shape(star);
            pg.pop();
        }
    }
}
