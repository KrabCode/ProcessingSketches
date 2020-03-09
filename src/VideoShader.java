import applet.KrabApplet;
import processing.core.PGraphics;
import processing.video.Movie;

/**
 * Created by Jakub 'Krab' Rak on 2020-03-08
 */
public class VideoShader extends KrabApplet {
    Movie movie;
    private PGraphics pg;

    public static void main(String[] args) {
        main("VideoShader");
    }

    public void settings() {
        size(800, 800, P2D);
//        fullScreen(P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        movie = new Movie(this, "videos/seaboat.mp4");
        movie.loop();
    }

    public void draw() {
        /*
        float second = .1f+frameCount/60f;
        movie.jump(second);
        movie.read();
        */
        pg.beginDraw();
        String shader = "video.glsl";
        uniform(shader).set("img", movie);
        hotFilter(shader, pg);
        pg.endDraw();

        image(pg, 0, 0);
        rec(pg);

        gui();
    }

    // Called every time a new frame is available to read
    public void movieEvent(Movie m) {
        m.read();
    }

}
