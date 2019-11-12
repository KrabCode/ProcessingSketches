import applet.juicygui.JuicyGuiSketch;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-09
 */
public class JuiceTest extends JuicyGuiSketch {
    private PGraphics pg;
    private float t;

    public static void main(String[] args) {
        JuicyGuiSketch.main("JuiceTest");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.noSmooth();
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        group("background");
        if(toggle("redraw", true)){
            pg.background(sliderFloat("fill", 0, 1000));
            pg.rectMode(CENTER);
        }

        if(button("bang")){
            println("bang");
        }

        group("translate");
        PVector translate = new PVector(
                sliderFloat("x", width*.5f, width*2),
                sliderFloat("y", width*.5f, height*2)
        );
        pg.translate(translate.x, translate.y);
        pg.rotate(sliderFloat("rotation", 0, TWO_PI));

        group("shape");
        pg.fill(sliderFloat("fill", 100, 1000));
        pg.stroke(sliderFloat("stroke", 255, 1000));
        pg.strokeWeight(sliderFloat("weight", 2, 100));
        float size = sliderFloat("size", 150, 1000);
        pg.pushMatrix();
        pg.translate(-size*.5f, -size*.5f);
        pg.beginShape();
        pg.vertex(0,0);
        pg.vertex(size,0);
        pg.vertex(size,size);
        pg.vertex(0,size);
        pg.endShape(CLOSE);
        pg.popMatrix();

        pg.text(radio("test", "testing"), 0, 0);

        pg.endDraw();
        image(pg, 0, 0);
        gui();
    }
}
