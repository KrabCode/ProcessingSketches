package boids;

public interface IParticle {
    void update();
    void display();
    boolean isDead();
}
