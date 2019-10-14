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
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Player> playersToRemove = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Bullet> bulletsToRemove = new ArrayList<>();
    private ArrayList<BloodEmitter> bloodEmitters = new ArrayList<>();
    private ArrayList<BloodEmitter> bloodEmittersToRemove = new ArrayList<>();
    private ArrayList<BloodParticle> bloodParticles = new ArrayList<BloodParticle>();
    private ArrayList<BloodParticle> bloodParticlesToRemove = new ArrayList<BloodParticle>();
    private float NULL_ANIMATION_FRAME = -1;

    public static void main(String[] args) {
        HotswapGuiSketch.main("TwinStickShooter");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.perspective();
        pg.endDraw();
        controllers = new ControllerManager();
        controllers.initSDLGamepad();
    }

    public void draw() {
        t += radians(slider("t", 1, true));
        patternPass(pg, t);
        pg.beginDraw();
        updatePlayers();
        updateBullets();
        updateBlood();
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui(false);
    }

    private void updateBlood() {
        for(BloodEmitter be : bloodEmitters){
            be.update();
        }
        bloodEmitters.removeAll(bloodEmittersToRemove);
        bloodEmittersToRemove.clear();
        for(BloodParticle bp : bloodParticles){
            bp.update();
        }
        bloodParticles.removeAll(bloodParticlesToRemove);
        bloodParticlesToRemove.clear();
    }

    private void updateBullets() {
        for (Bullet b : bullets) {
            b.update();
        }
        bullets.removeAll(bulletsToRemove);
        bulletsToRemove.clear();
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
        }
        players.removeAll(playersToRemove);
        playersToRemove.clear();
    }

    class Player {
        float r = 22, cooldownDuration = 20, cooldownStarted = NULL_ANIMATION_FRAME;
        PVector bodySize = new PVector(r * 1.5f, r * 2);
        PVector lookAt = PVector.fromAngle(random(2 * TWO_PI));
        PVector pos, headPos, spd = new PVector(), acc = new PVector();
        Weapon weapon = new Weapon();

        private float deathAnimationDuration = 60, deathAnimationStarted = NULL_ANIMATION_FRAME;

        Player(PVector spawnPos) {
            pos = spawnPos;
            headPos = new PVector(spawnPos.x, spawnPos.y);
        }

        public void update() {
            inputCheck();
            edgeCheck();
            updatePos();
            drawPlayer();
            updateDeathAnimation();
        }

        private void updateDeathAnimation() {
            if(deathAnimationStarted == NULL_ANIMATION_FRAME){
                return;
            }
            float deathAnimationNormalized = constrain(norm(frameCount, deathAnimationStarted, deathAnimationStarted + deathAnimationDuration), 0, 1);
            if (deathAnimationNormalized == 1) {
                playersToRemove.add(this);
            }
            pg.push();
            pg.translate(pos.x, pos.y);
            pg.fill(0);
            pg.noStroke();
            pg.ellipse(0,0,deathAnimationNormalized*r*2, deathAnimationNormalized*r*2);
            pg.pop();
        }

        private void drawPlayer() {
            drawBody();
            weapon.update(headPos, lookAt.heading());
            drawHead();
        }

        private void drawBody() {
            pg.push();
            pg.translate(pos.x, pos.y);
            pg.rotate(spd.heading());
            pg.noStroke();
            pg.fill(255, 100, 100);
            pg.ellipse(0, 0, bodySize.x, bodySize.y);
            pg.pop();
        }

        private void drawHead() {
            pg.push();
            pg.translate(headPos.x, headPos.y);
            pg.noStroke();
            pg.rectMode(PConstants.CORNER);
            pg.rotate(lookAt.heading());
            pg.fill(150, 60, 60);
            pg.arc(0, 0, r * 0.8f, r * 0.8f, -HALF_PI, HALF_PI);
            pg.fill(0);
            pg.arc(0, 0, r * 0.8f, r * 0.8f, HALF_PI, PI + HALF_PI);
            pg.pop();
        }

        private void inputCheck() {
            float inputMultiplier = slider("acc", 5);
            ControllerState controller = controllers.getState(players.indexOf(this));
            if (controller.leftStickMagnitude > .2) {
                acc.x += inputMultiplier * controller.leftStickX;
                acc.y -= inputMultiplier * controller.leftStickY;
            }
            if (controller.rightStickMagnitude > .2) {
                PVector newLookAt = PVector.fromAngle(radians(controller.rightStickAngle));
                newLookAt.y = -newLookAt.y;
                float angleDifference = angleDifference(lookAt.heading(), newLookAt.heading());
                lookAt.rotate(angleDifference * .1f);
            } else if (spd.mag() > .5f) {
                float angleDifference = angleDifference(lookAt.heading(), spd.heading());
                lookAt.rotate(angleDifference * .1f);
            }

            float cooldownNorm = norm(frameCount, cooldownStarted, cooldownStarted + cooldownDuration);
            if (cooldownNorm != NULL_ANIMATION_FRAME && cooldownNorm < 1) {
                return;
            }
            cooldownStarted = frameCount;

            if (controller.rb) {
                weapon.shoot(lookAt.heading());
            }
        }

        public float angleDifference(float angleStart, float angleTarget) {
            float d = angleTarget - angleStart;
            while (d > PI) {
                d -= TWO_PI;
            }
            while (d < -PI) {
                d += TWO_PI;
            }
            return d;
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

            headPos.x = lerp(headPos.x, pos.x, slider("head lerp"));
            headPos.y = lerp(headPos.y, pos.y, slider("head lerp"));
        }

        public boolean checkBulletHit(Bullet bullet) {
            float d = dist(bullet.pos.x, bullet.pos.y, pos.x, pos.y);
            if (d < min(bodySize.x, bodySize.y)) {
                takeDamage(bullet);
                return true;
            }
            return false;
        }

        private void takeDamage(Bullet bullet) {
            if(deathAnimationStarted != NULL_ANIMATION_FRAME){
                return;
            }
            deathAnimationStarted = frameCount;
            bloodEmitters.add(new BloodEmitter(new PVector(pos.x, pos.y), PVector.sub(bullet.pos, pos).heading(), deathAnimationDuration));
        }
    }

    class BloodEmitter {
        PVector pos;
        float sprayDirection, frameStarted, lifeDuration;

        public BloodEmitter(PVector pos, float heading, float lifeDuration) {
            this.pos = pos;
            this.lifeDuration = lifeDuration;
            sprayDirection = heading;
            frameStarted = frameCount;
        }

        void update() {
            float lifespanNormalized = constrain(norm(frameCount, frameStarted,frameStarted+lifeDuration), 0, 1);
            if(lifespanNormalized == 1){
                bloodEmittersToRemove.add(this);
            }
            bloodParticles.add(new BloodParticle(new PVector(pos.x, pos.y), sprayDirection+randomGaussian()*slider("blood gauss", .8f)));
        }

    }

    class BloodParticle {
        PVector pos, spd;
        float heading;
        private PVector size = new PVector(random(13), random(13));

        public BloodParticle(PVector pos, float heading) {
            this.pos = pos;
            this.heading = heading;
            this.spd = PVector.fromAngle(heading);
        }

        void update() {
            spd.setMag(slider("blood spd",3));
            spd.mult(.9f);
            pos.add(spd);

            if(min(size.x, size.y) < 0){
                bloodParticlesToRemove.add(this);
            }
            size.x -= .1f;
            size.y -= .1f;
            pg.push();
            pg.translate(pos.x, pos.y, pos.z);
            pg.rotate(heading);
            pg.fill(255,100,100);
            pg.noStroke();
            pg.ellipse(0,0,size.x, size.y);
            pg.pop();
        }
    }

    class Weapon {
        PVector bulletHole = new PVector();
        float bulletSpeed = slider("bullet speed", 50);
        PVector posOffset = new PVector(27, 18, 12);
        private PVector weaponSize = new PVector(35, 9.3f, 9);

        void update(PVector pos, float lookAt) {
            draw(pos, lookAt);
        }

        void draw(PVector pos, float lookAt) {
            pg.push();
            pg.translate(pos.x, pos.y);
            pg.rotate(lookAt);
            pg.translate(posOffset.x, posOffset.y, posOffset.z);
            pg.stroke(0);
            pg.fill(255);
            pg.box(weaponSize.x, weaponSize.y, weaponSize.z);
            bulletHole = new PVector(
                    pg.modelX(weaponSize.x * .35f, weaponSize.y * .25f, 0),
                    pg.modelY(weaponSize.x * .35f, weaponSize.y * .25f, 0),
                    pg.modelZ(weaponSize.x * .35f, weaponSize.y * .25f, 0)
            );
            pg.pop();
        }


        private void shoot(float lookAt) {
            Bullet b = new Bullet();
            b.pos = bulletHole;
            b.spd = PVector.fromAngle(lookAt).setMag(bulletSpeed);
            bullets.add(b);
        }
    }

    class Bullet {
        PVector pos, spd;

        void update() {
            pos.add(spd);
            if (!isPointInRect(pos.x, pos.y, -width, -height, width * 3, height * 3)) {
                bulletsToRemove.add(this);
            }
            for (Player p : players) {
                if (p.checkBulletHit(this)) {
                    bulletsToRemove.add(this);
                }
            }
            pg.push();
            pg.translate(pos.x, pos.y);
            pg.rotate(spd.heading());
            pg.stroke(255);
            pg.noFill();
            pg.box(10, 3, 3);
            pg.pop();
        }
    }
}


