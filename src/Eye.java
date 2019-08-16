import applet.GuiSketch;

public class Eye extends GuiSketch {

    public static void main(String[] args) {
        GuiSketch.main("Eye");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        colorMode(HSB, 1, 1, 1, 1);
    }

    float t;
    float tMod;
    float frameStartedRec = -1;
    float frameRecordingEnds = -1;

    public void keyPressed() {
        frameStartedRec = frameCount;
        frameRecordingEnds = frameStartedRec + 360 / tMod;
    }

    public void draw() {
        tMod = slider("t", 5);
        t += radians(tMod);
        background(.2f);

        translate(width*.5f, height*.5f);

        noStroke();
        fill(1);
        int borderCount = 1000;
        float w = slider("w", 20);
        for(int lidIndex = 0; lidIndex < 2; lidIndex++){
            beginShape(TRIANGLE_STRIP);
            for (int borderIndex = 0; borderIndex < borderCount; borderIndex++) {
                float n = map(borderIndex, 0, borderCount, 0, 1);
                float d = 1-abs(.5f-n)*2;
                float x = map(borderIndex, 0, borderCount-1, -300,300);
                float y = ease(d, slider("shape ease", 5))
                        * slider("d", 200)
                        * ease(constrain(abs(2*sin(t)), 0, 1), slider("open ease", 10));

                if(lidIndex == 0){
                    y = -y;
                }
                vertex(x,y-w/2);
                vertex(x,y+w/2);
            }
            endShape();
        }

        if (frameCount > frameStartedRec && frameCount <= frameRecordingEnds) {
            saveFrame(captureDir + "####.jpg");
        }

        gui();
    }
}
