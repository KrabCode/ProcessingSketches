import applet.GuiSketch;
import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-09
 */
public class JuiceTest extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        GuiSketch.main("JuiceTest");
    }

    public void settings() {
//        size(800, 800, P3D);
        fullScreen(P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.noSmooth();
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        group("background");
        if (button("reset once") || toggle("redraw", true)) {
            pg.hint(DISABLE_DEPTH_TEST);
            pg.noStroke();
            pg.fill(picker("fill"));
            pg.rectMode(CORNER);
            pg.rect(0, 0, width, height);
            pg.hint(ENABLE_DEPTH_TEST);
        }
        alphaFade(pg);
        rgbSplitPass(pg);
        group("matrix");
        String options = options("ortho", "perspective");
        if(options.equals("ortho")){
            pg.ortho();
        }else{
            pg.perspective();
        }
        PVector translate = sliderXYZ("translate", 1000);
        pg.translate(width*.5f + translate.x, height*.5f+ translate.y, translate.z);
        PVector rotate = sliderXYZ("rotate", 1).add(sliderXYZ("rotate speed", 1));
        pg.rotateX(rotate.x);
        pg.rotateY(rotate.y);
        pg.rotateZ(rotate.z);
        group("shape");
        if(toggle("nofill")){
            pg.noFill();
        }else{
            pg.fill(picker("fill", .5f, 1, .5f));
        }
        pg.stroke(picker("stroke", 0, 0, .8f));
        pg.strokeWeight(slider("weight", 2, 100));
        float size = slider("size", 150, 1000);
        pg.pushMatrix();
        String shape = options("rectangle", "ellipse", "box", "sphere");
        if ("rectangle".equals(shape)) {
            pg.rectMode(CENTER);
            pg.rect(0, 0, size, size);
        } else if ("ellipse".equals(shape)) {
            pg.ellipse(0, 0, size, size);
        } else if ("box".equals(shape)) {
            pg.box(size);
        } else if ("sphere".equals(shape)) {
            pg.sphereDetail(floor(slider("sphere detail", 10)));
            pg.sphere(size);
        }
        pg.popMatrix();
        pg.endDraw();
        background(0);
        image(pg, 0, 0);
        gui();
    }
}
