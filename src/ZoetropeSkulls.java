import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Created by Jakub 'Krab' Rak on 2020-03-15
 */
@SuppressWarnings("DuplicatedCode")
public class ZoetropeSkulls extends KrabApplet {
    PGraphics pg;
    PGraphics skullGraphics;
    PGraphics handsGraphics;
    float zoeTime = 0;
    PImage skullsImage;
    PImage handsImage;
    int currentFrameRate = 30;

    public static void main(String[] args) {
        KrabApplet.main("ZoetropeSkulls");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        for (int i = 0; i < 2; i++) {
            pg.beginDraw();
            pg.background(0);
            pg.endDraw();
        }
        skullGraphics = createGraphics(width, height, P2D);
        handsGraphics = createGraphics(width, height, P2D);
        skullsImage = loadImage("images/skulls.jpg");
        handsImage = loadImage("images/hands.jpg");
    }

    public void draw() {
        group("general");
        pg.beginDraw();
        pg.translate(width*.5f, height*.5f);
        String skullBg = "skullBg.glsl";
        uniform(skullBg).set("time", t);
        hotFilter(skullBg, pg);

        int frameSkip = sliderInt("frame skip", 6);
        if(frameCount % frameSkip == 0){
            zoeTime += radians(slider("rotation"));
        }

        group("skull");
        skullGraphics.beginDraw();
        skullGraphics.background(255);
        PVector translate = sliderXY("offset", 400, 400, 100);
        skullGraphics.translate(translate.x, translate.y);
        skullGraphics.rotate(zoeTime);
        skullGraphics.scale(slider("scale", 1));
        skullGraphics.imageMode(CENTER);
        skullGraphics.image(skullsImage, 0, 0);
        String brightPass = "brightPass.glsl";
        hotFilter(brightPass, skullGraphics);
        skullGraphics.endDraw();

        group("hands");

        handsGraphics.beginDraw();
        handsGraphics.background(255);
        PVector handsTranslate = sliderXY("global offset", 400, 400, 100);
        handsGraphics.translate(handsTranslate.x, handsTranslate.y);
        handsGraphics.rotate(zoeTime+slider("rotation offset"));
        PVector handsLocalTranslate = sliderXY("local offset", 400, 400, 100);
        handsGraphics.translate(handsLocalTranslate.x, handsLocalTranslate.y);
        handsGraphics.scale(slider("scale", 1));
        handsGraphics.imageMode(CENTER);
        handsGraphics.image(handsImage, 0, 0);
        hotFilter(brightPass, handsGraphics);
        handsGraphics.endDraw();

        pg.imageMode(CENTER);
        pg.image(skullGraphics, 0, 0);
        pg.image(handsGraphics, 0, 0);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
