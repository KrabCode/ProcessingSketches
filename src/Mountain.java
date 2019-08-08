import applet.ShadowGuiSketch;
import applet.GuiSketch;
import peasy.PeasyCam;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

// Do not rename class Mountain
// for it has been shared online
// and people would not find it
public class Mountain extends ShadowGuiSketch {
    public static void main(String[] args) {
        GuiSketch.main("Mountain");
    }

    float t;
    PeasyCam cam;
    ArrayList<Star> stars = new ArrayList<Star>();

    float baseWidth;
    float baseDepth;
    float maxAltitude;
    float sunDist;
    float detail = 60;

    float[][] fbmGrid = new float[floor(detail)][floor(detail)];
    float[][] noiseGrid = new float[floor(detail)][floor(detail)];

    int dayColor = color(85, 97, 150);
    int nightColor = color(0);
    private float nightBlackout;
    private float rockFrq;

    float tRecStart = -1;
    float tRecFinish = -1;

    public void keyPressed(){
        tRecStart = frameCount;
        tRecFinish = frameCount + 360*2;
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        super.setup();
        cam = new PeasyCam(this, 150);
        baseWidth = 200;
        baseDepth = 200;
        maxAltitude = 100;
        sunDist = maxAltitude * 1.2f;
    }

    public void draw() {
        t = HALF_PI+radians(frameCount * .5f);

        if (button("reset gui")) {
            resetGui();
        }
        if (button("reset seed")) {
            resetFbmGrid();
            resetNoiseGrid();
            noiseSeed(millis());
        }
        invalidateGrids();

        translate(0, maxAltitude * .5f);
        super.draw();
        stars();

        if(tRecStart > 0 && frameCount <= tRecFinish){
            saveFrame(captureDir+"####.jpg");
        }

        resetShader();
        noLights();
        gui();

        noStroke();
        fill(255);
        rect(0, 0, 50 * nightBlackout, 5);
    }

    private void invalidateGrids() {
        float oldDetail = detail;
        detail = slider("detail", 300);
        if (detail != oldDetail) {
            resetFbmGrid();
        }

        float oldRockFrq = rockFrq;
        rockFrq = slider("rock frq", 0, 1, .1f);
        if (oldRockFrq != rockFrq) {
            resetNoiseGrid();
        }
    }

    public void setLightDir(){
        lightDir.set(sunDist * sin(t), maxAltitude * .25f * cos(t), -sunDist * cos(t));
    }

    public void background(){
        background(lerpColor(dayColor, nightColor, .5f + .5f * cos(t)));
    }

    public void animate(PGraphics canvas) {
        float logicalCenter = (detail - 1) / 2f;
        float maxDistFromLogicalCenter = detail * .5f;
        nightBlackout = constrain(1 - cos(t), 0, 1);
        canvas.pushMatrix();
        canvas.noStroke();
        canvas.fill(0);
        for (int zIndex = 0; zIndex < detail; zIndex++) {
            canvas.beginShape(TRIANGLE_STRIP);
            for (int xIndex = 0; xIndex < detail; xIndex++) {
                float x = map(xIndex, 0, detail - 1, -baseWidth * .5f, baseWidth * .5f);
                float z0 = map(zIndex, 0, detail - 1, -baseDepth * .5f, baseDepth * .5f);
                float z1 = map(zIndex + 1, 0, detail - 1, -baseDepth * .5f, baseDepth * .5f);
                float d0 = 1 - constrain(map((dist(xIndex, zIndex, logicalCenter, logicalCenter)), 0, maxDistFromLogicalCenter, 0, 1), 0, 1);
                float d1 = 1 - constrain(map((dist(xIndex, zIndex + 1, logicalCenter, logicalCenter)), 0, maxDistFromLogicalCenter, 0, 1), 0, 1);
                float n0, n1;
                if (toggle("lock fbm", true)) {
                    n0 = getFbmAt(xIndex, zIndex);
                    n1 = getFbmAt(xIndex, zIndex + 1);
                } else {
                    n0 = fbm(xIndex, zIndex);
                    n1 = fbm(xIndex, zIndex + 1);
                }
                float y0 = -d0 * maxAltitude + maxAltitude * n0;
                float y1 = -d1 * maxAltitude + maxAltitude * n1;

                float rock0 = 150 * getNoiseAt(xIndex, zIndex);
                float gray0 = nightBlackout * (isSnow(y0, n0) ? 255 : rock0);
                canvas.fill(gray0);
                canvas.normal(x, y0, z0);
                canvas.vertex(x, y0, z0);

                float rock1 = 150 * getNoiseAt(xIndex, zIndex + 1);
                float gray1 = nightBlackout * (isSnow(y1, n1) ? 255 : rock1);
                canvas.fill(gray1);
                canvas.normal(x, y1, z1);
                canvas.vertex(x, y1, z1);
            }
            canvas.endShape(TRIANGLE_STRIP);
        }
        canvas.popMatrix();
    }

    private boolean isSnow(float y, float n) {
        return y < -maxAltitude / 2 + 2.5f * maxAltitude * n;
    }

    private void resetNoiseGrid() {
        noiseGrid = new float[ceil(detail)][ceil(detail)];
        for (int i = 0; i < detail; i++) {
            for (int j = 0; j < detail; j++) {
                noiseGrid[i][j] = -1;
            }
        }
    }

    private float getNoiseAt(int x, int y) {
        if (x < 0 || x >= noiseGrid.length || y < 0 || y >= noiseGrid.length) {
            return 0;
        }
        float val = noiseGrid[x][y];
        if (val == -1) {
            val = noise(x * rockFrq, y * rockFrq);
            noiseGrid[x][y] = val;
        }
        return val;
    }

    void resetFbmGrid() {
        fbmGrid = new float[ceil(detail)][ceil(detail)];
        for (int i = 0; i < detail; i++) {
            for (int j = 0; j < detail; j++) {
                fbmGrid[i][j] = -1;
            }
        }
    }

    float getFbmAt(int x, int y) {
        if (x < 0 || x >= fbmGrid.length || y < 0 || y >= fbmGrid.length) {
            return 0;
        }
        float val = fbmGrid[x][y];
        if (val == -1) {
            val = fbm(x, y);
            fbmGrid[x][y] = val;
        }
        return val;
    }

    float fbm(float x, float y) {
        float sum = 0;
        float frq = slider("freq", 0, 1, .05f);
        float amp = slider("amp", 0, 1, .4f);
        for (int i = 0; i < 6; i++) {
            sum += amp * (-1 + 2 * noise(x * frq, y * frq));
            frq *= slider("frq mod", 0, 5, 1.4f);
            amp *= slider("amp mod", .5f);
            x += 50;
            y += 50;
        }
        return abs(sum);
    }

    private void stars() {
        if (stars.isEmpty()) {
            for (int i = 0; i < 1000; i++) {
                stars.add(new Star());
            }
        }
        pushMatrix();
        rotateY(-t);
        for (Star s : stars) {
            s.update();
        }
        popMatrix();
    }

    class Star {
        PVector pos = PVector.random3D().setMag(maxAltitude * 2);
        float weight = random(1, 3);

        void update() {
            strokeWeight(weight);
            stroke(255, 255 * (.3f + .7f * cos(t)));
            noFill();
            point(pos.x, pos.y, pos.z);
        }
    }
}
