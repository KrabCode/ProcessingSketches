import applet.HotswapGuiSketch;
import processing.core.PGraphics;

public class Planet extends HotswapGuiSketch {

    PGraphics pg;
    private float t;
    String[] planet = new String[]{"planetFrag.glsl", "planetVert.glsl"};

    public static void main(String[] args) {
        HotswapGuiSketch.main("Planet");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        pg = createGraphics(width, height, P3D);
        surface.setAlwaysOnTop(true);
    }

    public void draw() {
        t += radians(slider("time"));
        pg.beginDraw();
        transparentWhitePass(pg);
        pg.translate(width*.5f,height*.5f);
        pg.rotateY(t);
        pg.stroke(0);
        hotShader(planet[0], planet[1], pg);
        pg.sphere(slider("r", 350));
        pg.endDraw();
        rec(pg);
        image(pg, 0,0,width,height);
        gui();
    }


}
