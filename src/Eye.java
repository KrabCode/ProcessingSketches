import applet.GuiSketch;

public class Eye extends GuiSketch {
    public static void main(String[] args) {
        GuiSketch.main("Eye");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {

    }
    float t;
    float tMod;
    float frameStartedRec = -1;
    float frameRecordingEnds = -1;

    public void keyPressed(){
        frameStartedRec = frameCount;
        frameRecordingEnds = frameStartedRec + 360 / tMod;
    }

    public void draw() {
        tMod = slider("t", 5);
        t += radians(tMod);
        background(0);

        float aCount = slider("a count", 800);
        float aStep = slider("a step", 6);
        stroke(255);
        strokeWeight(1);
        noFill();
        beginShape();
        for(int i = 0; i < aCount; i++){
            float a = i*aStep+t;
            float r = slider("r", 500);
            vertex(width*.5f+r*cos(a), height*.5f+ r*sin(a));
        }
        endShape();

        noStroke();
        int yCount = floor(slider("yCount", 80));
        float barHeight = width/(float)yCount;
        for(int yIndex = 0; yIndex < yCount; yIndex++){
            float y = map(yIndex, 0, yCount-1, 0, height);
            beginShape(TRIANGLE_STRIP);
            for(int x = -10; x < width+10; x+=5){
                float yDist = 1 - abs(width*.5f-x)*2 / width;
                yDist = ease(yDist, slider("dist ease", 0, 6, 2));
                if(y < height*.5f){
                    yDist = -yDist;
                }
                float finalY = y + yDist * ease(.5f+.5f*sin(t), slider("open ease", 10) )*slider("yDist range", 200);
                float centerDist = 1-map(dist(x,finalY, width*.5f, height*.5f), 0, width*.5f, 0, 1);
                fill(abs(yCount/2-yIndex)%2==0?255*centerDist:0);
                vertex(x,finalY);
                vertex(x,finalY+barHeight+5);
            }
            endShape();
        }

        if(frameCount > frameStartedRec && frameCount <= frameRecordingEnds){
            saveFrame(captureDir + "####.jpg");
        }

        gui();
    }
}
