import applet.HotswapGuiSketch;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Created by Jakub 'Krab' Rak on 2019-10-12
 */
public class TwinStickShooter extends HotswapGuiSketch {
    private ControllerManager controllers;
    private PGraphics pg;
    private float t;
    private ArrayList<Player> players = new ArrayList<Player>();

    public static void main(String[] args) {
        HotswapGuiSketch.main("TwinStickShooter");
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
        controllers = new ControllerManager();
        controllers.initSDLGamepad();
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        patternPass(pg, t);
        pg.beginDraw();
        updatePlayers();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui(false);
    }

    private void updatePlayers() {
        while (players.size() < controllers.getNumControllers()) {
            players.add(new Player(new PVector(
                    width * .5f + randomGaussian() * 50,
                    height * .5f + randomGaussian() * 50
            )));
        }
        while (players.size() > controllers.getNumControllers()) {
            players.remove(players.size() - 1);
        }

        for (Player player : players) {
            player.update();
            player.r = slider("r", 60);
        }
    }

    class Player {
        public float r = 50;
        PVector lookat = PVector.fromAngle(random(2*TWO_PI));
        PVector pos, spd = new PVector(), acc = new PVector();

        Player(PVector spawnPos) {
            pos = spawnPos;
        }

        public void update() {
            inputCheck();
            edgeCheck();
            updatePos();
            drawPlayer();
        }

        private void drawPlayer() {
            pg.pushMatrix();
            pg.translate(pos.x, pos.y);
            pg.noStroke();
            pg.fill(255,100, 100);
            pg.ellipse(0,0, r * 2, r * 2);
            pg.rectMode(PConstants.CORNER);
            pg.rotate(lookat.heading());
            for (int i = 1; i > 0; i-=2) {
                pg.pushMatrix();
                pg.translate(r*.5f, i*r*.5f);
                pg.fill(200);
                pg.rect(-r*.15f,-r*.15f, r*1.2f, r*.2f);
                pg.fill(100);
                pg.rect(-r*.15f,-r*.15f, r*0.4f, r*.2f);
                pg.popMatrix();
            }
            pg.fill(150,50,50);
            pg.arc(0, 0, r*0.8f, r*0.8f, -HALF_PI, HALF_PI);
            pg.fill(0);
            pg.arc(0, 0, r*0.8f, r*0.8f, HALF_PI, PI+HALF_PI);
            pg.popMatrix();
        }

        private void inputCheck() {
            float inputMultiplier = slider("acc", 5);
            ControllerState controller = controllers.getState(players.indexOf(this));
            if (controller.rightStickMagnitude > .2) {
                acc.x += inputMultiplier * controller.rightStickX;
                acc.y -= inputMultiplier * controller.rightStickY;
            }
            if(controller.leftStickMagnitude > .2){
                lookat = PVector.fromAngle(radians(controller.leftStickAngle));
                lookat.y = -lookat.y;
            }
        }

        private void edgeCheck() {
            float edgeBounce = 20;
            if (pos.x + r > width && spd.x > 0) {
                acc.x = -edgeBounce;
            }
            if (pos.x - r < 0 && spd.x < 0) {
                acc.x = edgeBounce;
            }
            if (pos.y + r > height && spd.y > 0) {
                acc.y = -edgeBounce;
            }
            if (pos.y - r < 0 && spd.y < 0) {
                acc.y = edgeBounce;
            }
        }

        private void updatePos() {
            spd.add(acc);
            spd.limit(slider("spd limit", 40));
            spd.mult(slider("drag", 0.f, 1.f, .8f));
            pos.add(spd);
            acc.mult(0);
        }
    }
}


