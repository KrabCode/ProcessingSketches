import applet.Sketch;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class Islamic extends Sketch {
    private PGraphics tex;
    private PImage src;
    private PVector offset;
    private float t;
    private float size = 1920 / 10;
    private float w = size * 2;
    private float h = sqrt(3) * size;
    private float xstep = w * (3 / 4f);
    private float ystep = h;
    private int hexCountX;
    private int hexCountY;
    private float equilateralTriangleHeight = sqrt(3) / 2;
    private int doubleClickInterval = 30;
    private int lastPressed = -doubleClickInterval * 2;
    private int frameStartedRec = -1;
    private int recordingDuration = 3000;

    public static void main(String[] args) {
        Sketch.main("Islamic");
    }

    public void settings() {
        size(600, 600, P2D);
//        fullScreen(P2D);
    }

    public void setup() {
        super.setup();
        offset = new PVector();
        hexCountX = floor(width / xstep + 2);
        hexCountY = floor(height / ystep + 2);
        tex = createGraphics(floor(size), floor(size), P2D);
        reset();
    }

    public void draw() {
        super.draw();
        t = map(frameCount, frameStartedRec, frameStartedRec + recordingDuration, 0, TWO_PI);
        background(0);
        updateTexture();
        drawHexagonGrid();

        if (frameStartedRec > 0 && frameCount < frameStartedRec + recordingDuration) {
            saveFrame(captureDir + "####.jpg");
        }

        drawTextureForDebugging();
    }

    public void keyPressed() {
        if (key == 'k') {
            frameStartedRec = frameCount + 1;
        }
        if (key == 'r') {
            reset();
        }
    }

    public void mousePressed() {
        if (lastPressed + doubleClickInterval > frameCount) {
            lastPressed = -doubleClickInterval * 2;
            reset();
        } else {
            lastPressed = frameCount;
        }
    }

    public void mouseDragged() {
        PVector m = new PVector(pmouseX - mouseX, pmouseY - mouseY);
        m.rotate(-t);
        offset.add(m);
    }

    void reset() {
        src = loadImage("https://picsum.photos/" + floor(size * 3) + ".jpg");

    }

    void drawHexagonGrid() {
        for (float xi = -hexCountX / 2; xi <= hexCountX / 2f; xi++) {
            for (float yi = -hexCountY / 2; yi <= hexCountY / 2f; yi++) {
                float x = width / 2f + xi * xstep;
                float y = height / 2f + yi * ystep - ystep / 2f;
                if (xi % 2 == 0) {
                    y += ystep / 2f;
                }
                pushMatrix();
                translate(x, y);
                drawHexagon();
                popMatrix();
            }
        }
    }

    void drawHexagon() {
        for (int triangleIndex = 0; triangleIndex <= 6; triangleIndex++) {
            float angle0 = map(triangleIndex, 0, 6, 0, TWO_PI);
            float angle1 = map(triangleIndex + 1, 0, 6, 0, TWO_PI);
            float x0 = size * cos(angle0);
            float y0 = size * sin(angle0);
            float x1 = size * cos(angle1);
            float y1 = size * sin(angle1);
            beginShape();
            noStroke();
            //strokeWeight(2);
            //stroke(255);
            textureMode(NORMAL);
            texture(tex);
            vertex(0, 0, 0.5f, 1 - equilateralTriangleHeight);
            if (triangleIndex % 2 == 0) { // mirror the texture every second triangle
                vertex(x0, y0, 0, 1);
                vertex(x1, y1, 1, 1);
            } else {
                vertex(x0, y0, 1, 1);
                vertex(x1, y1, 0, 1);
            }
            endShape();
        }
    }

    void updateTexture() {
        float w = tex.width;
        float h = tex.height;
        tex.beginDraw();
        tex.colorMode(HSB, 1, 1, 1, 1);
        tex.background(0);
        tex.imageMode(CENTER);
        tex.translate(w / 2, h / 2);
        tex.rotate(t);
        // tex.tint(.3);
        tex.image(src, offset.x, offset.y);
        tex.endDraw();
    }

    void drawTextureForDebugging() {
        pushMatrix();
        translate(tex.width * .6f, tex.height * .6f);
        rectMode(CENTER);
        noStroke();
        fill(0);
        rect(0, 0, tex.width * 1.2f, tex.height * 1.2f);
        imageMode(CENTER);
        image(tex, 0, 0);
        noFill();
        stroke(255, 0, 0);
        triangle(0, -(tex.height * equilateralTriangleHeight) / 2, -tex.width / 2, tex.height / 2, tex.width / 2, tex.height / 2);
        popMatrix();
    }
}
