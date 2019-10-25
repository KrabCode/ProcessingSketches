package unfinished;

import applet.GuiSketch;
import processing.core.PConstants;
import processing.core.PImage;

import java.util.ArrayList;

public class FaceAnimation extends GuiSketch {

    ArrayList<PImage> face, circle;
    int faceIndex = 0;
    int circleIndex = 0;
    private int frameRecordingEnds = -1;

    public static void main(String[] args) {
        GuiSketch.main("unfinished.FaceAnimation");
    }

    public void settings() {
        size(256,256, P2D);
    }

    public void setup() {
        face = loadImages("C:\\Projects\\ProcessingSketches\\data\\animation\\tvars_10fps\\face");
        circle = loadImages("C:\\Projects\\ProcessingSketches\\data\\animation\\tvars_10fps\\kruh");

        for(PImage f : face){
            f.filter(PConstants.INVERT);
        }
        for(PImage c : circle){
            c.filter(PConstants.INVERT);
        }
    }

    public void draw() {
        background(0);
        if (frameCount % floor(slider("speed", 1,20,6)) == 0) {
            circleIndex++;
            faceIndex++;
            circleIndex %= circle.size();
            faceIndex %= face.size();
        }
        translate(width*.5f,height*.5f);
        imageMode(CENTER);
        scale(slider("circle scale"));
        image(circle.get(circleIndex), 0, 0, width, height);
        scale(slider("face scale"));
        image(face.get(faceIndex), 0, 0, width, height);

        if(frameCount < frameRecordingEnds){
            saveFrame(captureFilename);
        }

        gui();
    }

    public void keyPressed(){
        frameRecordingEnds = frameCount+360;
    }
}
