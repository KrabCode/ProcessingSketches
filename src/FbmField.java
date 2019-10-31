import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

public class FbmField extends HotswapGuiSketch {
    public static void main(String[] args) {
        HotswapGuiSketch.main("FbmField");
    }

    private float t;
    private PGraphics pg;

    ArrayList<P> ps = new ArrayList<>();

    private float detail = 200;
    private float[][] fbmGrid = new float[floor(detail)][floor(detail)];

    private float freq = 0;
    private float amp = 0;
    private float freqMod = 0;
    private float ampMod = 0;

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        pg.beginDraw();
        alphaFade(pg);
        updateFbm(false);
        updateParticles();
        rgbSplitPass(pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void updateParticles() {
        int intendedParticleCount = floor(slider("count", 20000));
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

    private void updateFbm(boolean display) {
        freq = slider("freq", 0, .3f, .05f);
        amp = slider("amp", 0, 1, .4f);
        freqMod = slider("frq mod", 0, 5, 1.4f);
        ampMod = slider("amp mod", .5f);
        detail = floor(slider("detail", 800));
        if(button("reset fbm") || frameCount == 1){
            resetFbmGrid();
        }
        if(display){
            float size = width/(float)detail + 1;
            for (int xi = 0; xi < detail; xi++) {
                for (int yi = 0; yi < detail; yi++) {
                    float x = map(xi, 0, detail-1, 0, width);
                    float y = map(yi, 0, detail-1, 0, height);
                    float fbm = getFbmAt(xi, yi);
                    pg.rectMode(CORNER);
                    pg.noStroke();
                    pg.fill(fbm*255);
                    pg.rect(x,y,size,size);
                }
            }
        }
    }

    class P{
        PVector pos = new PVector(random(width), random(height));
        PVector prevPos = null;
        PVector spd = new PVector();

        void update(){
            int xi = floor(map(pos.x, 0, width, 0, detail));
            int yi = floor(map(pos.y, 0, height, 0, detail));
            PVector acc = PVector.fromAngle(4*PI*getFbmAt(xi, yi)).mult(slider("acc"));
            spd.add(acc);
            spd.mult(slider("drag", .8f, 1.f));
            pos.add(spd);
            boolean teleported = checkBounds();
            if(prevPos != null && !teleported){
                pg.strokeWeight(slider("w", 3.8f));
                pg.stroke(255, 255*slider("p alpha"));
                pg.line(pos.x, pos.y, prevPos.x, prevPos.y);
            }else{
                prevPos = new PVector();
            }
            prevPos.x = pos.x;
            prevPos.y = pos.y;
        }

        private boolean checkBounds() {
            boolean teleported = false;
            if (pos.x < 0) {
                pos.x += width;
                teleported = true;
            }
            if (pos.x > width) {
                pos.x -= width;
                teleported = true;
            }
            if (pos.y < 0) {
                pos.y += height;
                teleported = true;
            }
            if (pos.y > height) {
                pos.y -= height;
                teleported = true;
            }
            return teleported;
        }
    }



    private void resetFbmGrid() {
        fbmGrid = new float[ceil(detail)][ceil(detail)];
        for (int i = 0; i < detail; i++) {
            for (int j = 0; j < detail; j++) {
                fbmGrid[i][j] = -1;
            }
        }
    }

    private float getFbmAt(int x, int y) {
        if (x < 0 || x >= fbmGrid.length || y < 0 || y >= fbmGrid.length) {
            return 0;
        }
        float val = fbmGrid[x][y];
        if (val == -1) {
            val = fbm(x, y);
            fbmGrid[x][y] = val;
        }
        return val;
    }

    private float fbm(float x, float y) {
        float sum = 0;
        float amp = this.amp;
        float freq = this.freq;
        for (int i = 0; i < 6; i++) {
            sum += amp * (-1 + 2 * noise(x * freq, y * freq));
            freq *= freqMod;
            amp *= ampMod;
            x += 50;
            y += 50;
        }
        return .5f+.5f*sum;
    }
}
