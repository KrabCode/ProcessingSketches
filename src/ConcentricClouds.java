import applet.GuiSketch;
import applet.HotswapGuiSketch;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.ArrayList;

public class ConcentricClouds extends HotswapGuiSketch {
    ArrayList<Cloud> clouds = new ArrayList<Cloud>();
    ArrayList<Cloud> cloudsToRemove = new ArrayList<Cloud>();
    PImage[] images;
    float intendedCloudCount;
    private float t;
    private int recordingEnds;

    public static void main(String[] args) {
        GuiSketch.main("ConcentricClouds");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        images = new PImage[]{
                loadImage("data/concentric/oriznute/chubb0.png"),
                loadImage("data/concentric/oriznute/chubb1.png"),
                loadImage("data/concentric/oriznute/chubb2.png"),
                loadImage("data/concentric/oriznute/chubb3.png"),
                loadImage("data/concentric/oriznute/chubb23.png"),
                loadImage("data/concentric/oriznute/eased1.png")
        };
        colorMode(HSB, 1, 1, 1);
    }

    public void draw() {
        t += radians(slider("t", 0, 1, .12f));
        background();
        updateClouds();
        filter();
        if(frameCount < recordingEnds){
            saveFrame(captureFilename);
        }
        gui();
    }

    public void keyPressed(){
        recordingEnds = frameCount + 360;
    }

    private void background() {
        String background = "concentric/background.glsl";
        uniform(background).set("time", t);
        hotFilter(background);
    }

    private void filter() {
        String filter = "concentric/filter.glsl";
        uniform(filter).set("time", t);
        hotFilter(filter);
    }

    private void updateClouds() {
        intendedCloudCount = floor(slider("cloud count", 30));
        if (frameCount % 30 == 0 && clouds.size() < intendedCloudCount) {
            clouds.add(new Cloud());
        }
        while(clouds.size() > intendedCloudCount){
            clouds.remove(0);
        }
        translate(width * .5f, height * .5f);
        for (Cloud c : clouds) {
            c.update();
            pushMatrix();
            if (c.toRemove()) {
                cloudsToRemove.add(c);
            }
            rotate(c.rotPos);
            scale(c.scl, c.scl);
            imageMode(CENTER);
            image(c.pg, 0, 0);
            popMatrix();
        }

        clouds.removeAll(cloudsToRemove);
        cloudsToRemove.clear();
    }

    private PImage randomImage() {
        int randomIndex = floor(random(images.length));
        return images[randomIndex];
    }

    class Cloud {
        PGraphics pg;
        String foregroundShader = "concentric/cloud.glsl";
        PImage img;
        float scl = 0;
        private float rotPos = randomGaussian();
        private float rotSpd = abs(randomGaussian() * .005f);

        Cloud() {
            this.img = randomImage();
            pg = createGraphics(img.width, img.height, P2D);
        }

        void update() {
            rotPos += rotSpd;
            scl += slider("scale speed", 0, .01f, 0.001f);
            pg.beginDraw();

            uniform(foregroundShader).set("time", t);
            hotShader(foregroundShader, pg);
            pg.imageMode(CENTER);
            pg.translate(pg.width * .5f, pg.height * .5f);
            pg.image(img, 0, 0);
            pg.endDraw();
        }

        boolean toRemove() {
            return scl > 2;
        }
    }
}
