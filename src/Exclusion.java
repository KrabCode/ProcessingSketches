import applet.GuiSketch;
import processing.opengl.PShader;

public class Exclusion extends GuiSketch {
    private float t;
    private PShader fxaa;
    float tRecStart = -1;
    float tRecFinish = -1;

    public static void main(String[] args) {
        GuiSketch.main("Exclusion");
    }

    public void settings() {
        size(800, 800, P2D);
//        fullScreen(P2D);
    }

    public void setup() {
        fxaa = new PShader(this, "fxaaVert.glsl", "fxaaFrag.glsl");
        colorMode(HSB, 1, 1, 1, 1);
    }

    public void draw() {
        t += radians(1);
        background(0);
        noStroke();

        translate(width * .5f, height * .5f);
        int rings = floor(slider("r count", 120));
        for (int rIndex = 0; rIndex < rings; rIndex++) {
            float r = map(rIndex, 0, rings - 1, 0, width);
            int aCount = floor(slider("a count", 50));
            for (int aIndex = 0; aIndex < aCount; aIndex++) {
                if(rIndex%2==0){
                    blendMode(ADD);
                }
                else{
                    blendMode(SUBTRACT);
                }
                float a = map(aIndex, 0, aCount, 0, TWO_PI);
                float x = r*cos(a);
                float y = r*sin(a);
                float d = (r/width)*slider("hue dist", 20);
                fill(d%1, slider("s",0,1,1), slider("b",0,1,.1f));
                float timeRadius = slider("time radius", 360);
                float size = slider("timeBase",1000)+timeRadius*sin(t);
                ellipse(x,y, size,size);
            }
        }

        blendMode(BLEND);

        if (toggle("fxaa")) {
            for (int i = 0; i < slider("passes", 0, 30, 2); i++) {
                filter(fxaa);
            }
        } else {
            resetShader();
        }

        if (tRecStart > 0 && frameCount <= tRecFinish) {
            saveFrame(captureDir + "####.jpg");
        }

        gui();
    }

    public void keyPressed() {
        tRecStart = frameCount;
        tRecFinish = frameCount + 360;
    }

}
