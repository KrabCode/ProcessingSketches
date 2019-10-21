import applet.HotswapGuiSketch;
import processing.core.PFont;
import processing.core.PGraphics;

import java.util.HashMap;

public class Text extends HotswapGuiSketch {
    private int fontSizeStep = 1, fontSizeMin = 16, fontSizeMax = 100;

    public static void main(String[] args) {
        HotswapGuiSketch.main("Text");
    }

    private float t;
    private PGraphics pg;
    private HashMap<Integer, PFont> fonts = new HashMap<Integer, PFont>();
    String userInputText = "text";

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        for (int i = fontSizeMin; i < fontSizeMax; i += fontSizeStep) {
            fonts.put(i, createFont("data/fonts/dejavu/DejaVuSansMono.ttf", i));
        }
        pg.endDraw();
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        pg.colorMode(HSB, 1,1,1,1);
        pg.beginDraw();
        pg.background(0);
        pg.textAlign(CENTER, CENTER);
        pg.translate(width * .5f, height * .5f);
        pg.scale(slider("scale", 2));
        for (int i = fontSizeMin; i < fontSizeMax; i += fontSizeStep) {
            float inorm = 1 - norm(i, 4, 100);
            pg.push();
            if (toggle("global")) {
                pg.rotate(inorm * slider("angle", -TWO_PI, TWO_PI));
            } else {
                pg.rotate(slider("amp", 5) * sin(inorm * TWO_PI * slider("freq", -TWO_PI, TWO_PI)));
            }
            pg.textFont(fonts.get(i));
            if (i % 2 == 0) {
                pg.fill(0);
            } else {
                float hueStart = slider("hue start");
                float hueRange = slider("hue range");
                pg.fill(hueStart+inorm*hueRange,1.f,1.f-inorm);
            }
            pg.text(userInputText, 0, 0);
            pg.pop();
        }
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    @Override
    public void keyPressed() {
        if (keyCode == BACKSPACE) {
            if (userInputText.length() > 0) {
                userInputText = userInputText.substring(0, userInputText.length() - 1);
            }
        } else if (keyCode == DELETE) {
            userInputText = "";
        } else if (keyCode != SHIFT && keyCode != CONTROL && keyCode != ALT) {
            userInputText = userInputText + key;
        }
    }
}
