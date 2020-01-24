import applet.KrabApplet;
import processing.core.PGraphics;

import java.util.ArrayList;

public class ParallaxTerrain extends KrabApplet {
    public static void main(String[] args) {
        ParallaxTerrain.main("ParallaxTerrain");
    }

    private PGraphics pg;
    ArrayList<ArrayList<Float>> levelHistories = new ArrayList<ArrayList<Float>>();

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        frameRecordingDuration *= 10;
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        parallaxTerrain();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    float fbm(float x, int octaves, float freq, float amp, float freqMult, float ampMult){
        float sum = 0;
        for (int i = 0; i < octaves; i++) {
            sum += amp*(1-2*noise(x*freq));
            freq *= freqMult;
            amp *= ampMult;
            x += 13.123f;
        }
        return sum;
    }

    private void parallaxTerrain() {
        group("global");
        int levels = sliderInt("level count", 4);
        while(levelHistories.size() < levels){
            levelHistories.add(new ArrayList<>());
        }
        while(levelHistories.size() > levels){
            levelHistories.remove(levelHistories.size()-1);
        }
        for(int i = 0; i < levels; i++){
            group("level " + i);
            ArrayList<Float> history = levelHistories.get(i);
            int historySize = sliderInt("history size");
            float newValue = fbm(t*slider("time"), sliderInt("octaves", 3),
                    slider("freq", 1), slider("amp", 1),
                    slider("freq mult", 2), slider("amp mult", .5f));
            history.add(newValue);
            while(history.size() > historySize){
                history.remove(0);
            }
            pg.beginShape(TRIANGLE_STRIP);
            pg.strokeWeight(slider("weight", 1));
            pg.stroke(picker("stroke").clr());
            pg.fill(picker("fill").clr());
            if(toggle("no stroke")){
                pg.noStroke();
            }
            if(toggle("no fill")){
                pg.noFill();
            }
            float baseY = slider("y");
            float bufferZone = 50;
            for(int j = 0; j < history.size(); j++){
                float f = history.get(j);
                float x = map(j, 0, history.size()-1, -bufferZone, width+bufferZone);
                pg.vertex(x, baseY + f);
                pg.vertex(x, height+bufferZone);
            }
            pg.endShape();
        }

    }

}
