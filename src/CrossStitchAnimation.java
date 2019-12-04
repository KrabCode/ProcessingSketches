import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class CrossStitchAnimation extends KrabApplet {
    private PGraphics crossTex;
    private PGraphics pg;

    public static void main(String[] args) {
        CrossStitchAnimation.main("CrossStitchAnimation");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        generateCrossTexture();
        group("draw");
        pg.beginDraw();
        colorFilter(pg);
        alphaFade(pg);
        splitPass(pg);
        pg.translate(width * .5f, height * .5f);
//        PVector rot = sliderXYZ("rotate").add(sliderXYZ("rotate spd"));
        PVector rot = new PVector(0, t, 0);
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z);
        pg.stroke(picker("stroke").clr());
        pg.fill(picker("fill").clr());
        pg.strokeWeight(slider("w"));
        float r = slider("r", 200);
        pg.box(r);
        pg.endDraw();
        background(0);
        image(pg, 0, 0);
        group("cross");
        if(toggle("stitch", true)){
            crossStitchPass(g);
        }
        if(toggle("preview")){
            image(crossTex, width-crossTex.width*2, 0, crossTex.width*2, crossTex.height*2);
        }

        rec(g);
        gui();
    }

    private void generateCrossTexture() {
        group("cross");
        crossTex = createGraphics(32, 32, P2D);
        crossTex.beginDraw();
        crossTex.clear();
        crossTex.colorMode(HSB, 1, 1, 1, 1);
        crossTex.noFill();
        float w = slider("weight");
        HSBA stroke = picker("stroke");
        float margin = slider("margin");
        crossTex.pushMatrix();
        crossTex.translate(crossTex.width * .5f, crossTex.height * .5f);
        for (int i = 0; i < 2; i++) {
            float x = (crossTex.width - margin) * .5f;
            float y = (crossTex.height - margin) * .5f;
            if (i > 0) {
                y *= -1;
            }
            crossTex.strokeWeight(w);
            crossTex.stroke(stroke.clr());
            crossTex.line(-x, -y, x, y);

        }
        crossTex.popMatrix();
        crossTex.endDraw();
    }

    private void crossStitchPass(PGraphics pg) {
        String crossStitch = "crossStitch.glsl";
        uniform(crossStitch).set("crossTexture", crossTex);
        uniform(crossStitch).set("pixelSize", slider("pixel size", 10));
        hotFilter(crossStitch, pg);
    }
}
