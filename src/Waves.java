import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import utils.OpenSimplexNoise;

/**
 * Created by Jakub 'Krab' Rak on 2019-11-24
 */
public class Waves extends KrabApplet {
    private OpenSimplexNoise noise = new OpenSimplexNoise();
    int gridSize = 10;
    Particle[][] grid;
    private PGraphics pg;
    private float baseAmp = 1;

    public static void main(String[] args) {
        KrabApplet.main("Waves");
    }

    public void settings() {
        fullScreen(P3D);
//        size(800, 800, P3D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P3D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
        group("particles");
        Particle p = new Particle(0, 0);
        p.update();
        resetGrid();
        frameRecordingDuration *= 2;
    }

    public void draw() {
        pg.beginDraw();
        group("main");
        PVector tran = sliderXYZ("translate");
        PVector rot = sliderXYZ("rotate");
        alphaFade(pg);
        splitPass(pg);
        pg.translate(width*.5f+tran.x,height*.5f+tran.y, tran.z);
        pg.rotateX(rot.x);
        pg.rotateY(rot.y);
        pg.rotateZ(rot.z+=radians(slider("z rotation", 1)));
        updateGrid();
        pg.endDraw();
        background(0);
        image(pg, 0, 0);
        rec(pg);
        gui();
        if(frameCount == 5){
            loadLastStateFromFile(true);
        }
    }

    private void resetGrid() {
        grid = new Particle[gridSize][gridSize];
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                grid[x][y] = new Particle(x,y);
            }
        }
    }

    private void updateGrid(){
        group("particles");
        int intendedGridSize = sliderInt("grid size");
        if (button("reset") || intendedGridSize != gridSize) {
            gridSize = intendedGridSize;
            resetGrid();
        }
        pg.pushMatrix();
        pg.translate(0, 0, -averageZ()*slider("average z correct", 1));
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Particle particle = grid[x][y];
                particle.update();
            }
        }
        pg.popMatrix();
    }

    private float averageZ() {
        float sum = 0;
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                sum += grid[x][y].pos.z;
            }
        }
        sum /= gridSize*gridSize;
        return sum;
    }

    private float fbm(int x, int y) {
        group("fbm");
        int octaves = sliderInt("octaves", 6);
        float sum = 0;
        float freq = slider("base freq", 1);
        baseAmp = slider("base amp", 1);
        float amp = baseAmp;
        float tr = slider("time radius x");
        for (int i = 0; i < octaves; i++) {
            sum += amp*noise.eval(30+x*freq+tr*cos(t),42+y*freq+tr*sin(t));
            x += TWO_PI * 7.5f;
            y += TWO_PI * 11.3f;
            freq *= slider("next freq");
            amp *= slider("next amp");
        }
        return 1-abs(-1+2*sum);
    }

    class Particle{
        PVector gridPos, pos = new PVector();
        private float hueOffset = random(-1,1);

        public Particle(int x, int y) {
            gridPos = new PVector(x, y);
        }

        void update() {
            group("particles");
            float gridSizeInPixels = slider("grid pixels", 300);
            pos.x = map(gridPos.x, 0, gridSize, -gridSizeInPixels*.5f, gridSizeInPixels*.5f);
            pos.y =  map(gridPos.y, 0, gridSize, -gridSizeInPixels*.5f, gridSizeInPixels*.5f);
            float d = norm(pos.mag(), 0, gridSizeInPixels*.5f);
            if(d > 1){
                return;
            }
            pos.z = fbm(floor(gridPos.x),floor(gridPos.y))*(1-d*slider("distance fbm faloff"));
            group("particles");
            pg.strokeWeight(slider("weight", 1.9f));
            HSBA hsba = picker("stroke");
            pg.colorMode(HSB, 1, 1, 1, 1);
            float z = norm(pos.z, 0, baseAmp);
            pg.stroke(
                    hueModulo(hsba.hue()+z*slider("z hue")+hueOffset*slider("rand hue")),
                    hsba.sat() + z * slider("sat"),
                    hsba.br(),
                    hsba.alpha()
            );
            pg.point(pos.x, pos.y, pos.z);
        }
    }
}
