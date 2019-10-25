package unfinished;

import applet.HotswapGuiSketch;
import com.studiohartman.jamepad.ControllerManager;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Created by Jakub 'Krab' Rak on 2019-10-18
 */
public class Magnify extends HotswapGuiSketch {

    /*
    * TODO
    *  - generative grid city with roads, crossroads and city blocks, (movable camera?)
    *  - simple car physics, rudimentary car AI
    *  - human flocking
    *  - textures for water zones, human preferred walking zones / walkable zones / safe zones where people disappear
    *  - burn with magnifier, first tint red, then maybe black and then replace texture with scorched texture
    * */

    PVector burningFocus = new PVector();
    PVector burningFocusPrev = new PVector();
    ControllerManager controllers = new ControllerManager();
    private PGraphics pg;
    private PGraphics backgroundScorchMap;
    private boolean burning = false;
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("unfinished.Magnify");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        backgroundScorchMap = createGraphics(width, height, P2D);
        backgroundScorchMap.beginDraw();
        backgroundScorchMap.background(0);
        backgroundScorchMap.endDraw();
        controllers.initSDLGamepad();
    }

    public void draw() {
        t += radians(slider("t", 0, 1, 1));
        registerInput();
        pg.beginDraw();
        updateBackground();
//        updateForeground();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }

    private void registerInput() {
        burning = mousePressedOutsideGui || controllers.getState(0).x;
        burningFocusPrev.x = burningFocus.x;
        burningFocusPrev.y = burningFocus.y;
        float focusLerp = .05f;
        burningFocus.x = lerp(burningFocus.x, mouseX, focusLerp);
        burningFocus.y = lerp(burningFocus.y, mouseY, focusLerp);
    }

    private void updateBackground() {
        backgroundScorchMap.beginDraw();
        float strokeGrayscale = slider("stroke", 30);
        if (burning) {
            float normD = constrain(norm(PVector.dist(burningFocus, burningFocusPrev), 0, 20), 0, 1);
            backgroundScorchMap.strokeWeight(10 - 8 * normD);
            backgroundScorchMap.blendMode(ADD);
            backgroundScorchMap.stroke(strokeGrayscale);
            backgroundScorchMap.line(burningFocus.x, burningFocus.y, burningFocusPrev.x, burningFocusPrev.y);
        }
        String magnifyBackgroundShader = "magnifyBg.glsl";
        backgroundScorchMap.endDraw();
        uniform(magnifyBackgroundShader).set("scorchMap", backgroundScorchMap);
        uniform(magnifyBackgroundShader).set("time", t);
        hotFilter(magnifyBackgroundShader, pg);
    }

    /*
    private void updateForeground(){
        drawPeople();
        drawBuildings();
        drawCars();
        drawMagnifyingGlass();
        drawBurningFocus();
    }
*/
}
