import applet.KrabApplet;
import processing.core.PVector;

public class GridFizzbuzz extends KrabApplet {
    public static void main(String[] args) {
        GridFizzbuzz.main("GridFizzbuzz");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
    }

    public void draw(){
        background(0);
        drawGrid();
    }

    void drawGrid(){
        int columns = 10;
        int rows = 10;
        int i = 0;
        PVector size = new PVector(width/(float)columns, height/(float)rows);
        for (int yi = 0; yi < rows; yi++) {
            for (int xi = 0; xi < columns; xi++) {
                i++;
                float x = map(xi, 0, columns, 0, width);
                float y = map(yi, 0, rows, 0, height);
                stroke(60);
                fill(cellColor(i));
                rect(x,y,size.x, size.y);
                fill(255);
                textAlign(CENTER, CENTER);
                textSize(size.x * .3f);
                text(cellTextTop(i), x+size.x/2,y+size.y/2-size.y*.2f);
                textSize(size.x * .2f);
                text(cellTextBottom(i), x+size.x/2,y+size.y/2+size.y*.2f);
            }
        }
    }

    String cellTextTop(int i){
        return String.valueOf(i);
    }

    String cellTextBottom(int i){
        if(i % 6 == 0){
            return "fizzbuzz";
        }
        if(i % 2 == 0){
            return "fizz";
        }
        if(i % 3 == 0){
            return "buzz";
        }
        return "";
    }

    int cellColor(int i){
        return color(0);
    }
}
