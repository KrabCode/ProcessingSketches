import applet.GuiSketch;
import applet.HotswapGuiSketch;
import peasy.PeasyCam;
import processing.core.PVector;
import processing.opengl.PShader;

import java.util.ArrayList;

public class Stars extends HotswapGuiSketch {
    private ArrayList<Star> stars = new ArrayList<Star>();
    private int bgDark;
    private PShader rgbSplit;
    private float depth;

    public static void main(String[] args) {
        GuiSketch.main("Stars");
    }

    public void settings() {
        fullScreen(P3D);
    }

    public void setup() {
        bgDark = color(0, 0);
        depth = width;
        new PeasyCam(this, depth);
    }

    public void draw() {
        background(bgDark);
        float t = radians(frameCount);
        updateDrawStars(t);
        chromaticAberrationPass(g);
        rec();
        gui();
    }

    private void updateDrawStars(float t) {
        int starCount = 3000;
        while (stars.size() < starCount) {
            stars.add(new Star());
        }
        for (Star s : stars) {
            s.update(t);
        }
    }

    public void keyPressed() {
        saveFrame(captureFilename);
    }

    class Star {
        PVector pos;
        float weight = 1.5f + abs(randomGaussian());

        private float fadeInDuration = 30;
        float fadeInStarted = -fadeInDuration * 2;
        private float lastZ;

        Star() {
            float x = randomGaussian() * width * .5f;
            float y = randomGaussian() * width * .5f;
            pos = new PVector(x, y, random(depth));
            lastZ = pos.z;
        }

        void update(float t) {
            float timeOffset = map(t, 0, TWO_PI, 0, depth);
            float z = (pos.z + timeOffset) % depth;
            if (lastZ > z) {
                fadeInStarted = frameCount;
            }
            lastZ = z;
            float fadeInNormalized = map(frameCount, fadeInStarted, fadeInStarted + fadeInDuration, 0, 1);
            stroke(lerpColor(bgDark, color(255), fadeInNormalized));
            strokeWeight(weight);
            point(pos.x, pos.y, z);
        }
    }
}
