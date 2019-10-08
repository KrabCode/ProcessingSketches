package utils;

import applet.HotswapGuiSketch;

import java.util.HashMap;

public class KeypressMonitor extends HotswapGuiSketch {
    HashMap<Character, Boolean> keys = new HashMap<Character, Boolean>();

    public static void main(String[] args) {
        HotswapGuiSketch.main("utils.KeypressMonitor");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void keyPressed() {
        keys.put(key, true);
    }

    public void keyReleased() {
        keys.put(key, false);
    }

    boolean isPressed(Character key) {
        return keys.containsKey(key) && keys.get(key);
    }

    public void draw() {
        StringBuilder allKeys = new StringBuilder();
        for (Character key : keys.keySet()) {
            if (isPressed(key)) {
                allKeys.append(key);
            }
        }
        background(0);
        fill(255);
        textAlign(CENTER, CENTER);
        textSize(30);
        text(allKeys.toString(), width / 2, height / 2);
    }

}
