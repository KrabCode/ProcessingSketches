import applet.Sketch;
import processing.core.PGraphics;
import processing.opengl.PShader;

public class NormalSea extends Sketch {
    PShader shade;
    PShader sobel;
    PShader blur;
    PShader hmap;
    PGraphics main;
    PGraphics normalMap;
    PGraphics heightMap;
    int diffuse;
    float t;
    int frameStart = 0;
    int gifDurationInFrames = 300;
    float shininess, scl, sobelStrength, sobelLevel, blurSize, r;

    public static void main(String[] args) {
        Sketch.main("NormalSea");
    }

    public void settings() {
        size(800, 800, P3D);
        smooth(8);
    }

    public void setup() {
        main = createGraphics(width, height, P3D);
        shade = loadShader("NkingPhongFrag.glsl", "NkingPhongVert.glsl");
        sobel = loadShader("sobel.glsl");
        blur = loadShader("blur.glsl");
        hmap = loadShader("hmap.glsl");
        heightMap = createGraphics(width, height, P2D);
        heightMap.beginDraw();
        heightMap.background(0);
        heightMap.endDraw();
        normalMap = createGraphics(width, height, P2D);
        updateNormal();
    }

    public void draw() {
        super.draw();
        t = map(frameCount, frameStart, frameStart + gifDurationInFrames, 0, TWO_PI);
        sobel.set("strength", sobelStrength);
        sobel.set("level", sobelLevel);
        drawHeight();
        updateNormal();
        drawMain(0,0,90,255,242,213);
        image(main, 0, 0);
        sliders();
        capture();
        gui();
    }

    private void sliders() {
        shininess = slider("shininess", 0, 100, 3.5f);
        scl = slider("scl", 0, 50, 15);
        sobelStrength = slider("sobel strength", 0, 4, 3);
        sobelLevel = slider("sobel level", 0, 8, 2);
        blurSize = slider("blur size", 0, 8);
        r = slider("loop radius", 0, 5, 1);
    }

    private void capture() {
        if (frameStart != 0 && frameCount <= frameStart + gifDurationInFrames) {
            saveFrame(captureDir+"/####.jpg");
        }
    }

    public void keyPressed() {
        frameStart = frameCount;
    }

    void drawHeight() {
        hmap.set("z", r * cos(t));
        hmap.set("w", r * sin(t));
        hmap.set("scl", scl);
        heightMap.beginDraw();
        heightMap.background(0);
        heightMap.shader(hmap);
        heightMap.rectMode(CORNER);
        heightMap.noStroke();
        heightMap.rect(0, 0, width, height);
        heightMap.endDraw();
    }

    void updateNormal() {
        normalMap.beginDraw();
        normalMap.background(0);
        normalMap.image(heightMap, 0, 0);
        normalMap.filter(sobel);
        blur.set("blurSize", blurSize);
        blur.set("sigma", 2.f);
        blur.set("horizontal", true);
        normalMap.filter(blur);
        blur.set("horizontal", false);
        normalMap.filter(blur);
        normalMap.endDraw();
    }

    void drawMain(int ambientR, int ambientG, int ambientB, int specR, int specG, int specB) {
        main.beginDraw();
        main.stroke(255);
        main.noStroke();
        main.fill(255);
        main.background(0);
        main.ambientLight(ambientR, ambientG, ambientB);
        main.lightSpecular(specR,specG,specB);
        float x = mouseX;
        float y = mouseY;
        main.directionalLight(255, 255, 255, 1 - 2 * (x / (float) width), 1 - 2 * (y / (float) height), -1);
        main.specular(specR, specG, specB);
        main.shininess(shininess);
        main.ambient(ambientR, ambientG, ambientB);
        diffuse = color(0);
        main.shader(shade);
        main.beginShape(QUAD);
        main.textureMode(NORMAL);
        main.texture(normalMap);
        main.attrib("diffuse", red(diffuse) / 255f, green(diffuse) / 255f, blue(diffuse) / 255f, 1.0f);
        main.normal(0, 0, 1);
        main.attribNormal("tangent", 1, 0, 0);
        main.vertex(0, 0, 0, 1);
        main.vertex(main.width, 0, 1, 1);
        main.vertex(main.width, main.height, 1, 0);
        main.vertex(0, main.height, 0, 0);
        main.endShape();
        main.endDraw();
    }
}
