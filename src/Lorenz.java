import applet.GuiSketch;
import peasy.PeasyCam;
import processing.core.PVector;

import java.util.ArrayList;

public class Lorenz extends GuiSketch {
    public static void main(String[] args) {
        GuiSketch.main("Lorenz");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    ArrayList<Point> path = new ArrayList<Point>();

    float x = .01f;
    float y = 0.f;
    float z = 0.f;

    float a = 10;
    float b = 28;
    float c = 8/3f;

    int cyan, magenta;

    public void setup() {
        colorMode(RGB,1,1,1,1);
        cyan = color(0,1,1);
        magenta = color(1,0,1);
        new PeasyCam(this, 200);
    }

    public void draw() {
        background(0);
        float t = radians(frameCount*.1f);
        int clr = lerpColor(cyan, magenta, .5f+.5f*sin(t));
        float dt = slider("dt", .01f);
        float dx = a * (y - x);
        float dy = x * (b - z) - y;
        float dz = x * y - c * z;
        x += dx*dt;
        y += dy*dt;
        z += dz*dt;
        path.add(new Point(new PVector(x,y,z), clr));

        while(path.size() > slider("count", 3000)){
            path.remove(0);
        }

        beginShape();
        noFill();
        strokeWeight(slider("weight", 0, 3));
        for(Point p : path){
            stroke(p.color);
            PVector n = new PVector(
                    -1+2*noise(p.pos.x, p.pos.y, p.pos.z),
                    -1+2*noise(10+p.pos.x, 10+p.pos.y, 10+p.pos.z),
                    -1+2*noise(30+p.pos.x, 30+p.pos.y, 30+p.pos.z)
            );
            n.setMag(slider("mag", 3));
            vertex(p.pos.x+n.x, p.pos.y+n.y, p.pos.z+n.z);
        }
        endShape();

        gui();
    }

    class Point{
        PVector pos;
        int color;
        Point(PVector pos, int color){
            this.pos = pos;
            this.color = color;
        }
    }
}
