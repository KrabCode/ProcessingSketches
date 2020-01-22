import applet.KrabApplet;
import processing.core.PGraphics;

public class ShaderParticles extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        ShaderParticles.main("ShaderParticles");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        surface.setLocation(1920 - 820, 20);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        String particles = "particles.glsl";
        uniform(particles).set("time", t);
        hotFilter(particles, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
