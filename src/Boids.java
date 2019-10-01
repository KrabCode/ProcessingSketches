import applet.HotswapGuiSketch;
import processing.core.PVector;

import java.util.ArrayList;

public class Boids extends HotswapGuiSketch {

    //TODO
    // - pretty birds / fish / whatever
    // - angle of sight for behaviors with nice debug arcs
    // - efficient division, compute shaders?
    // - camera moving with player
    // - food chain
    // - player takes control of eater when eaten
    // - player gets bigger as he eats

    private PVector cameraPos;
    private PVector cameraOffset;

    private ArrayList<Boid> boids = new ArrayList<Boid>();
    private ArrayList<Boid> toRemove = new ArrayList<Boid>();

    public static void main(String[] args) {
        HotswapGuiSketch.main("Boids");
    }

    public void settings() {
        size(800, 800, P2D);
        cameraPos = new PVector();
        cameraOffset = new PVector(width*.5f, height*.5f);
    }

    public void setup() {
        Boid player = new Boid();
        player.pos = new PVector(width*.5f, height*.5f);
        player.isPlayer = true;
        boids.add(player);
    }

    public void draw() {
        bg();
        updateCamera();
        updateBoids();
        rec();
        gui();
    }

    private void bg() {
        background(0);
    }

    private void updateCamera() {
        Boid player = getPlayer();
        cameraPos.x = lerp(cameraPos.x, player.pos.x, slider("lerp"));
        cameraPos.y = lerp(cameraPos.y, player.pos.y, slider("lerp"));
        println(cameraPos, player.pos);
        if(mousePressedOutsideGui){
            cameraPos.x += mouseX-pmouseX;
            cameraPos.y += mouseY-pmouseY;
        }
        translate(cameraPos.x-cameraOffset.x, cameraPos.y-cameraOffset.y);
    }

    private Boid getPlayer() {
        for(Boid b : boids){
            if(b.isPlayer){
                return b;
            }
        }
        throw new IllegalStateException("No player found in boids list");
    }

    private void updateBoids() {
        toRemove.clear();
        int intendedBoidCount = floor(slider("count",1, 30));
        while(boids.size() < intendedBoidCount){
            boids.add(new Boid());
        }
        while(boids.size() > intendedBoidCount){
            boids.remove(boids.size()-1);
        }
        for(Boid b : boids){
            moveTowardsFlock();
            avoidTouch();
            alignDirection();
            display(b);
        }
    }

    private void display(Boid b) {
        if(b.isPlayer){
            stroke(255,0,0);
        }else{
            stroke(255);
        }
        strokeWeight(3);
        point(b.pos.x, b.pos.y);
        if(b.dead){
            toRemove.add(b);
        }
    }

    private void alignDirection() {

    }

    private void avoidTouch() {

    }

    private void moveTowardsFlock() {

    }

    private float directionTowardsPlayer() {
        return 0;
    }

    private PVector positionInFrontOfPlayer() {
        return new PVector(random(width), random(height));
    }

    private class Boid {
        PVector pos = positionInFrontOfPlayer();
        float dir = random(TWO_PI);
        boolean dead = false;
        boolean isPlayer = false;
    }


}
