import applet.Sketch;

public class MountainSunset extends Sketch {
    public static void main(String[] args) {
        Sketch.main("MountainSunset");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        super.setup();
    }

    public void draw() {
        super.draw();
        float t = radians(frameCount);
        if (button("reset")) {
            resetGui();
        }
        sky();
        mountain(width * 1.5f, height * 1.5f, 500, slider("detail", 0,100, 40));
        particles();
        gui();
    }

    void sky() {
        background(140, 190, 214);
    }

    void mountain(float baseWidth, float baseHeight, float altitude, float detail) {
        pushMatrix();
        translate(width * .5f, height + baseHeight * .1f);
        rotateX(-HALF_PI);
        noStroke();
        float logicalCenter = (detail - 1) / 2f;
        float maxDistFromLogicalCenter = detail * .5f;
        for (int yIndex = 0; yIndex < detail; yIndex++) {
            beginShape(TRIANGLE_STRIP);
            for (int xIndex = 0; xIndex < detail; xIndex++) {
                float x = map(xIndex, 0, detail - 1, -baseWidth * .5f, baseWidth * .5f);
                float y0 = map(yIndex, 0, detail - 1, 0, baseHeight * .5f);
                float y1 = map(yIndex + 1, 0, detail - 1, 0, baseHeight * .5f);
                float d0 = 1 - constrain(map((dist(xIndex, yIndex, logicalCenter, logicalCenter)), 0, maxDistFromLogicalCenter, 0, 1), 0, 1);
                float d1 = 1 - constrain(map((dist(xIndex, yIndex + 1, logicalCenter, logicalCenter)), 0, maxDistFromLogicalCenter, 0, 1), 0, 1);
                float n0 = fbm(xIndex, yIndex);
                float n1 = fbm(xIndex, yIndex + 1);
                float z0 = -d0 * altitude + altitude * n0;
                float z1 = -d1 * altitude + altitude * n1;
                fill(z0+noise(x,y0) > -slider("snowcap", 0, 400, 300) ? 100-255 * n0 : 255);
                vertex(x, y0, z0);
                fill(z1+noise(x,y1) > -slider("snowcap") ? 100-255 * n1 : 255);
                vertex(x, y1, z1);
            }
            endShape(TRIANGLE_STRIP);
        }
        popMatrix();
    }

    float fbm(float x, float y) {
        float sum = 0;
        float frq = slider("freq", 0, 1, .12f);
        float amp = slider("amp", 1);
        for (int i = 0; i < 6; i++) {
            sum += amp * (-1 + 2 * noise(x * frq, y * frq));
            frq *= slider("frq mod", 0, 5, 1.64f);
            amp *= slider("amp mod", .5f);
            x += 50;
            y += 50;
        }
        return abs(sum);
    }

    void particles() {

    }
}
