import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Lissajous3D extends KrabApplet {

    PGraphics pg;
    float time = 0;

    public static void main(String[] args) {
        KrabApplet.main("Lissajous3D");
    }

    @Override
    public void settings() {
        size(800,800,P3D);
    }

    @Override
    public void setup() {
        surface.setLocation(1920-820,20);
        colorMode(HSB,1,1,1,1);
        pg = createGraphics(width, height, P3D);
    }

    @Override
    public void draw() {
        time += radians(slider("t", 1));
        pg.beginDraw();
        alphaFade(pg);
        rgbSplitPass(pg);
        splitPass(pg);
        PVector tran = sliderXYZ("translate");
        pg.translate(width*.5f+tran.x, height*.5f+tran.y, tran.z);
        PVector rot = sliderXYZ("rotation").add(sliderXYZ("rotation speed"));
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z);
        drawLissajous3D();
        pg.endDraw();
        image(pg, 0, 0, width, height);
        rec(pg);
        gui();
    }

    private void drawLissajous3D() {
        int count = sliderInt("count");

        PVector amp = sliderXYZ("amplitude", 100, 1000);
        PVector frq = sliderXYZ("frequence",1, 10);

        pg.beginShape(POINTS);
        pg.noFill();
        pg.strokeWeight(slider("weight"));
        pg.stroke(picker("stroke").clr());
        for (int i = 0; i < count; i++) {
            float inorm = norm(i, 0, count-1);
            float angle = inorm*TAU*sliderInt("taus")+time;
            float x = amp.x*cos(angle*frq.x);
            float y = amp.y*sin(angle*frq.y);
            float z = amp.z*cos(angle*frq.z);
            pg.vertex(x,y,z);
        }
        pg.endShape();
    }
}
