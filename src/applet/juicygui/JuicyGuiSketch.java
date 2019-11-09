package applet.juicygui;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

@SuppressWarnings({"InnerClassMayBeStatic", "SameParameterValue"})
public class JuicyGuiSketch extends PApplet {
    //TODO
    // ----- Operation JUICE -----
    // gui elements:
    // keyboard, mouse, mousewheel controls
    // optional dynamic hide-able categories
    // juicy minimalist sliders with tooltip value
    // juicy buttons, visually clear on/off states
    // hue picker slider
    // 2D vector unit grid picker
    // radio options with string result
    // ...
    // other features:
    // unique group / name combinations, same name can appear multiple times in different groups
    // proper margins and sizes, no code salad
    // infinite sliders, define precision instead of min/max (division by 0 problem)
    // lock other gui elements when one is being used, unlock one frame after mouse release to avoid unintentional input
    // action click animation for extra juice
    // two or three buttons/toggles to a row
    // print state on state change
    // honor existing interface, only extend it
    // ...
    // implementation:
    // gui element class
    // standalone slider, button, toggle, hue, grid, dropdown
    // tray class
    // category class
    // row class

    private ArrayList<Group> groups = new ArrayList<>();
    private Group parentGroup = null; //do not access directly!
    private float cell = 40;
    private float minimumTrayWidth = cell * 6;
    private boolean trayVisible = true;
    private boolean pMousePressed;

    private int keyboardSelectionIndex = 0;
    private String keyboardAction;
    private boolean keyboardInputActive;
    private int specialKeyCount = 3;

    private float fillTextSelected = 1;
    private float fillText = .6f;
    private float fontSize = 24;

    private boolean overlayVisible;
    private Element overlayOwner;

    public static boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }

    protected void gui() {
        gui(false);
    }

    protected void gui(boolean defaultVisibility) {
        if(frameCount == 1){
            trayVisible = defaultVisibility;
        }
        pushStyle();
        colorMode(HSB, 1, 1, 1, 1);
        resetMatrixInAnyRenderer();
        updateTrayBackground();
        updateSpecialButtons();
        updateGroupsAndElementsInTray();
        updateFps();
        updateOverlay();
        popStyle();
        pMousePressed = mousePressed;
        keyboardAction = "";
    }

    private void updateFps() {
        if(!trayVisible){
            return;
        }
        float x = cell*.25f;
        float y = height-cell;
        fill(fillText);
        textAlign(CENTER, CENTER);
        int nonFlickeringFrameRate = floor(frameRate > 58 && frameRate < 62 ? 60 : frameRate);
        text(nonFlickeringFrameRate + " fps", x,y,cell*2, cell);
    }

    private void updateOverlay() {
        if(!overlayVisible){
            return;
        }

    }

    private void updateSpecialButtons() {
        float x = 0;
        float y = 0;
        textSize(fontSize);
        textAlign(CENTER, BOTTOM);
        updateSpecialHideButton(x, y, cell * 2, cell);
        if (!trayVisible) {
            return;
        }
        x += cell * 2;
        updateSpecialUndoButton(x, y, cell * 2, cell);
        x += cell * 2;
        updateSpecialRedoButton(x, y, cell * 2, cell);
    }

    private void updateSpecialHideButton(float x, float y, float w, float h) {
        if (mouseJustReleased(x, y, w, h) || (keyboardInput("hide/show") && keyboardAction.equals("ACTION"))) {
            trayVisible = !trayVisible;
        }
        fill(keyboardInput("hide/show") ? fillTextSelected : fillText);
        text(trayVisible ? "hide" : "show", x, y, w, h);
    }

    private void updateSpecialUndoButton(float x, float y, float w, float h) {
        fill(keyboardInput("undo") ? fillTextSelected : fillText);
        text("undo", x, y, w, h);
    }

    private void updateSpecialRedoButton(float x, float y, float w, float h) {
        fill(keyboardInput("redo") ? fillTextSelected : fillText);
        text("redo", x, y, w, h);
    }

    private boolean keyboardInput(String query) {
        if(!trayVisible){
            keyboardSelectionIndex = 0;
        }
        if (!keyboardInputActive) {
            return false;
        }
        if (query.equals("hide/show") && keyboardSelectionIndex == 0) {
            return true;
        }
        if (query.equals("undo") && keyboardSelectionIndex == 1) {
            return true;
        }
        if (query.equals("redo") && keyboardSelectionIndex == 2) {
            return true;
        }
        int i = specialKeyCount;
        for (Group group : groups) {
            if(group.name.equals(query) && keyboardSelectionIndex == i){
                return true;
            }
            i++;
            for(Element el : group.elements){
                if(el.name.equals(query) && keyboardSelectionIndex == i){
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    int keyboardSelectionIndexMax() {
        int elementCount = 0;
        for (Group group : groups) {
            elementCount += group.elements.size();
        }
        return specialKeyCount + groups.size() + elementCount;
    }

    public void mousePressed() {
        keyboardInputActive = false;
    }

    public void keyPressed() {
        if (key == CODED) {
            if (keyCode == UP) {
                if(keyboardSelectionIndex == specialKeyCount){
                    keyboardSelectionIndex = 0;
                }else{
                    keyboardSelectionIndex--;
                }
            } else if (keyCode == DOWN) {
                if(keyboardSelectionIndex < specialKeyCount){
                    keyboardSelectionIndex = specialKeyCount;
                }else{
                    keyboardSelectionIndex++;
                }
            } else if (keyCode == LEFT) {
                if(keyboardSelectionIndex > 0 && keyboardSelectionIndex < specialKeyCount){
                    keyboardSelectionIndex--;
                }else{
                    keyboardAction = "LEFT";
                }
            } else if (keyCode == RIGHT) {
                if(keyboardSelectionIndex < specialKeyCount){
                    keyboardSelectionIndex++;
                }else{
                    keyboardAction = "RIGHT";
                }
            }
            if (keyboardSelectionIndex < 0) {
                keyboardSelectionIndex = keyboardSelectionIndexMax();
            }
            keyboardSelectionIndex %= keyboardSelectionIndexMax();
        }
        if (key == ' ' || key == ENTER) {
            keyboardAction = "ACTION";
        }
        keyboardInputActive = true;
    }

    private boolean mouseJustReleased(float x, float y, float w, float h) {
        return pMousePressed && !mousePressed && isPointInRect(mouseX, mouseY, x, y, w, h);
    }

    protected void group(String name) {
        Group group = findGroup(name);
        if (!groupExists(name)) {
            group = new Group(name);
            groups.add(group);
        }
        setParentGroup(group);
    }

    private void updateGroupsAndElementsInTray() {
        if (!trayVisible) {
            return;
        }
        float x = cell * .5f;
        float y = cell * 3;
        for (Group group : groups) {
            textAlign(LEFT, BOTTOM);
            fill(keyboardInput(group.name) ? fillTextSelected : fillText);
            textSize(fontSize);
            text(group.name, x, y);
            x += cell * .5f;
            for (Element el : group.elements) {
                y += cell;
                fill(keyboardInput(el.name) ? fillTextSelected : fillText);
                text(el.name, x, y);
            }
            x -= cell * .5f;
            y += cell;
        }
    }

    protected void updateTrayBackground() {
        if (!trayVisible) {
            return;
        }
        float trayWidth = max(minimumTrayWidth, findLongestNameWidth() + cell * 2);
        float backgroundAlpha = .5f;
        noStroke();
        fill(0, backgroundAlpha);
        rectMode(CORNER);
        rect(0, 0, trayWidth, height);
        stroke(.3f, backgroundAlpha);
        for (float x = cell; x < trayWidth; x += cell) {
            line(x, 0, x, height);
        }
        for (float y = cell; y < height; y += cell) {
            line(0, y, trayWidth, y);
        }
    }

    private float findLongestNameWidth() {
        float longestNameWidth = 0;
        for (Group group : groups) {
            for (Element el : group.elements) {
                float nameWidth = textWidth(el.name);
                if (nameWidth > longestNameWidth) {
                    longestNameWidth = nameWidth;
                }
            }
        }
        return longestNameWidth;
    }

    private void resetMatrixInAnyRenderer() {
        if (sketchRenderer().equals(P3D)) {
            camera();
        } else {
            resetMatrix();
        }
    }

    private Group getParentGroup() {
        if (parentGroup == null) {
            Group anonymous = new Group("");
            groups.add(anonymous);
            parentGroup = anonymous;
        }
        return parentGroup;
    }

    private void setParentGroup(Group parentGroup) {
        this.parentGroup = parentGroup;
    }

    private Group findGroup(String name) {
        for (Group group : groups) {
            if (group.name.equals(name)) {
                return group;
            }
        }
        return null;
    }

    private boolean groupExists(String name) {
        return findGroup(name) != null;
    }

    private Element findElementInGroup(String name, Group parentGroup) {
        for (Element el : parentGroup.elements) {
            if (el.name.equals(name)) {
                return el;
            }
        }
        return null;
    }

    private boolean groupContainsElement(String name, Group parentGroup) {
        return findElementInGroup(name, parentGroup) != null;
    }

    protected int sliderInt(String name, int defaultValue, int defaultPrecision) {
        if (!groupContainsElement(name, getParentGroup())) {
            parentGroup.elements.add(new SliderInt(name, defaultValue, defaultPrecision));
        }
        SliderInt slider = (SliderInt) findElementInGroup(name, parentGroup);
        return defaultValue;
    }

    class Group {
        String name;
        boolean expanded = true;
        float x,y,w,h;
        ArrayList<Element> elements = new ArrayList<>();

        public Group(String name) {
            this.name = name;
        }

        public Group(String name, boolean expanded) {
            this.name = name;
            this.expanded = expanded;
        }
    }

    class Element{
        float x,y,w,h;
        String name;
    }

    class SliderInt extends Element {
        int value, precision;

        public SliderInt(String name, int defaultValue, int defaultPrecision) {
            this.name = name;
            this.value = defaultValue;
            this.precision = defaultPrecision;
        }
    }

    class Slider1D extends Element {
        float value, precision;
    }

    class Slider2D extends Element {
        PVector value = new PVector();
        float precision;
    }

    class SliderColor extends Element {
        float hue, saturation, brightness;
    }

    class Radio extends Element {
        String[] options;
    }

    class Button extends Element {
        boolean justPressed = false;
    }

    class Toggle extends Element {
        boolean state = false;
    }
}
