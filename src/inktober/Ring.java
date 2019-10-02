package inktober;

import applet.HotswapGuiSketch;
import processing.core.PGraphics;

public class Ring extends HotswapGuiSketch {
    private float t;
    PGraphics pg;

    public static void main(String[] args) {
        HotswapGuiSketch.main("inktober.Ring");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        pg = createGraphics(width, height, P2D);
        recordingFrames *= 2;
    }

    public void draw() {
        t += radians(slider("t"));
        pg.beginDraw();
        alphaFade(pg);
        pg.translate(width*.5f, height*.5f);
        pg.rotate(t);
        float r = slider("centerR", 500);
        triangleStrip(pg, r, true);
        triangleStrip(pg, r, false);
        pg.endDraw();
        rgbSplitPass(pg);
        noisePass(t, pg);
        rec(pg);
        image(pg, 0, 0, width, height);
        gui();
    }

    private void triangleStrip(PGraphics pg, float r, boolean outward) {
        float w = slider("w", 100);
        int detail = floor(slider("detail", 100));
        pg.beginShape(TRIANGLE_STRIP);
        for(int i = 0; i < detail; i++){
            float inorm = norm(i, 0, detail-1);
            float a = inorm*TWO_PI;
            pg.noStroke();
            pg.fill(inorm*255,255);
            pg.vertex(r*cos(a), r*sin(a));
            pg.fill(inorm*255,0);
            if(outward){
                pg.vertex((r+w)*cos(a), (r+w)*sin(a));
            }else{
                pg.vertex((r-w)*cos(a), (r-w)*sin(a));
            }
        }
        pg.endShape();
    }
}
