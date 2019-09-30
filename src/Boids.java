import applet.HotswapGuiSketch;
import processing.core.PVector;

import java.util.ArrayList;

public class Boids extends HotswapGuiSketch {
    private ArrayList<Boid> boids = new ArrayList<Boid>();
    private ArrayList<Boid> toRemove = new ArrayList<Boid>();

    public static void main(String[] args) {
        HotswapGuiSketch.main("Boids");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {

    }

    public void draw() {
        updateBoids();
        rec();
        gui();
    }

    private void updateBoids() {
        toRemove.clear();
        int intendedBoidCount = floor(slider("count",1, 500));
        while(boids.size() < intendedBoidCount){
            boids.add(new Boid());
        }
        while(boids.size() > intendedBoidCount){
            boids.remove(boids.size()-1);
        }
        for(Boid b : boids){
            //TODO
            // - pretty birds / fish / whatever
            // - angle of sight for behaviors with nice debug arcs
            // - efficient division, compute shaders?
            // - camera moving with player
            // - food chain
            // - player takes control of eater when eaten
            // - player gets bigger as he eats

            moveTowardsFlock();
            avoidTouch();
            alignDirection();
            point(b.pos.x, b.pos.y);
            if(b.dead){
                toRemove.add(b);
            }
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
        float dir = directionTowardsPlayer();
        boolean dead;
    }


}
