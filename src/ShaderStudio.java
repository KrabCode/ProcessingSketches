import applet.GuiSketch;
import applet.HotswapGuiSketch;

public class ShaderStudio extends HotswapGuiSketch {
    private float t = 0;
    private int recordingEnds;

    public static void main(String[] args) {
        GuiSketch.main("ShaderStudio");
    }

    public void settings() {
//        size(800, 800, P2D);
        fullScreen(P2D, 2);
    }

    public void setup() {

    }

    public void draw() {
        t += radians(slider("t", 0, 1, 1));
        background(0);
        String shaderPath = "frag.glsl";
        uniform(shaderPath).set("time", t);
        hotFilter(shaderPath);
        if(frameCount < recordingEnds){
            saveFrame(captureDir+"####.jpg");
        }
        gui(false);
    }

    public void keyPressed(){
        if(key == 'k'){
            regenId();
            recordingEnds = frameCount+360;
        }
    }

}
