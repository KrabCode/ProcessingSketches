import applet.KrabApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.HashMap;

public class CrossStitch extends KrabApplet {
    private PImage src, posterized;
    private int posterizeLevel = 2;
    private int pixelCount;
    private int w, h;
    private int[][] pattern;
    private ArrayList<Integer> patternColors = new ArrayList<Integer>();
    private PGraphics pg;
    private boolean posterizeSettingsChanged;
    private String colorFindMode = "";
    private int smoothRadius = 1;

    public static void main(String[] args) {
        CrossStitch.main("CrossStitch");
    }

    public void settings() {
        size(1000,1000, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
    }

    public void draw() {
        pg.beginDraw();
        pg.background(0);
        updateSource();
        updatePattern();
        String draw = options( "source", "poster", "pattern");
        if (draw.equals("source")) {
            pg.image(src, 0, 0, width, height);
        } else if (draw.equals("poster")) {
            pg.image(posterized, 0, 0, width, height);
        } else {
            drawPattern();
        }
        pg.endDraw();
        image(pg, 0, 0);
        colorFilter();
        rec(pg);
        gui();
    }

    private void updateSource() {
        boolean newImageLoaded = false;
        posterizeSettingsChanged = false;
        if(button("load clouds") || frameCount == 1){
            src = loadImage("images/cloudy.jpg");
            newImageLoaded = true;
        }
        if (button("load random")) {
            src = loadImage(randomImageUrl(width, height));
            newImageLoaded = true;
        }
        if(button("grayscale")){
            src.filter(GRAY);
            newImageLoaded = true;
        }
        int intendedPosterizeLevel = sliderInt("posterize level", 2, 50, 8);
        if (newImageLoaded || posterizeLevel != intendedPosterizeLevel) {
            posterizeLevel = intendedPosterizeLevel;
            posterized = src.copy();
            posterized.filter(POSTERIZE, posterizeLevel);
            posterizeSettingsChanged = true;
        }
    }

    private void updatePattern() {
        int intendedPixelCount = sliderInt("resolution", 100);
        if(pixelCount != intendedPixelCount){
            pixelCount = intendedPixelCount;
            w = pixelCount;
            h = pixelCount;
            posterizeSettingsChanged = true;
        }

        String intendedColorFindMode = options("smooth color", "simple color");
        if(!colorFindMode.equals(intendedColorFindMode)){
            colorFindMode = intendedColorFindMode;
            posterizeSettingsChanged = true;
        }

        int intendedSmoothRadius = sliderInt("smooth radius", 2, 255, 0);
        if(smoothRadius != intendedSmoothRadius){
            smoothRadius = intendedSmoothRadius;
            posterizeSettingsChanged = true;
        }
        if(!posterizeSettingsChanged){
            return;
        }

        pattern = new int[w][h];
        patternColors.clear();
        posterized.loadPixels();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int canvasX = floor(map(x + .5f, 0, w - 1, 0, posterized.width));
                int canvasY = floor(map(y + .5f, 0, w - 1, 0, posterized.height));
                if (colorFindMode.equals("simple color")) {
                    pattern[x][y] = posterized.get(canvasX, canvasY);
                } else {
                    pattern[x][y] = getMostRepresentedColorInCircle(posterized, canvasX, canvasY, smoothRadius);
                }
                if(!patternColors.contains(pattern[x][y])){
                    patternColors.add(pattern[x][y]);
                }
            }
        }
        println("colors used",patternColors.size());
    }

    private int getMostRepresentedColorInCircle(PImage img, int x, int y, int radius) {
        if(radius < 1){
            return color(0);
        }
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int i = x - radius; i < x + radius; i++) {
            for (int j = y - radius; j < y + radius; j++) {
                if (i < 0 || i >= width || j < 0 || j >= height) {
                    continue;
                }
                if (dist(x, y, i, j) > radius) {
                    continue;
                }
                colors.add(img.get(i, j));
            }
        }
        return mostRepresentedColor(colors);
    }

    private int mostRepresentedColor(ArrayList<Integer> colors) {
        HashMap<Integer, Integer> colorCounts = new HashMap<Integer, Integer>();
        for (Integer color : colors) {
            if (!colorCounts.containsKey(color)) {
                colorCounts.put(color, 1);
            } else {
                colorCounts.put(color, colorCounts.get(color) + 1);
            }
        }
        int mostRepresentedColor = color(0);
        int mostPixelsUsedByAColor = 0;
        for (Integer color : colorCounts.keySet()) {
            int pixelsUsed = colorCounts.get(color);
            if (pixelsUsed > mostPixelsUsedByAColor) {
                mostRepresentedColor = color;
                mostPixelsUsedByAColor = pixelsUsed;
            }
        }
        return mostRepresentedColor;
    }

    private void drawPattern() {
        int crossSizeX = ceil(width / (float) w);
        int crossSizeY = ceil(height / (float) h);
        for (int xi = 0; xi < w; xi++) {
            for (int yi = 0; yi < h; yi++) {
                float x = map(xi, 0, w - 1, 0, width);
                float y = map(yi, 0, h - 1, 0, height);
                pg.noStroke();
                pg.fill(pattern[xi][yi]);
                pg.rect(x, y, crossSizeX *1.1f, crossSizeY *1.1f);
            }
        }
    }
}
