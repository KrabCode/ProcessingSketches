import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class CrossStitchAnimation extends KrabApplet {
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
        frameRecordingDuration *= 2;
    }

    public void draw() {
        group("draw");
        pg.beginDraw();
        colorFilter(pg);
        if(toggle("fade")){
            alphaFade(pg);
        }else{
            alphaAdd(pg);
        }
        splitPass(pg);

        pg.translate(width * .5f, height * .5f);
//        PVector rot = sliderXYZ("rotate").add(sliderXYZ("rotate spd"));
        PVector rot = new PVector(toggle("X")?t:0, toggle("Y")?t:0, toggle("Z")?t:0);
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z);

        spiralSphere(pg);

        pg.endDraw();
        background(0);
        image(pg, 0, 0);
        group("cross");
        if(toggle("stitch", true)){
            crossStitchPass(g);
        }

        rec(g);
        gui();
    }

    private void crossStitchPass(PGraphics pg) {
        String crossStitch = "crossStitch.glsl";
        uniform(crossStitch).set("pixelSize", slider("pixel size", 10));
        hotFilter(crossStitch, pg);
    }
}
