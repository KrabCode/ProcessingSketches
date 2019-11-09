package applet.juicygui;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

@SuppressWarnings({"InnerClassMayBeStatic", "SameParameterValue", "FieldCanBeLocal"})
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

    // color
    float backgroundAlpha = .5f;
    private boolean onPC;
    private boolean justStarted = true;
    private ArrayList<Group> groups = new ArrayList<>();
    private Group parentGroup = null; //do not access directly!
    private float fillBackground = .3f;
    private float fillTextSelected = 1;
    private float fillText = .6f;
    private float fontSize = 24;

    // input
    private boolean pMousePressed = false;
    private int keyboardSelectionIndex = 0;
    private String keyboardAction;
    private boolean keyboardInputActive;
    private int specialKeyCount = 3;

    // layout
    private boolean trayVisible = true;
    private float cell = 40;
    private float minimumTrayWidth = cell * 6;
    private float trayWidth;

    //overlay
    private boolean overlayVisible;
    private Element overlayOwner;

    public static boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }

    protected void gui() {
        gui(false);
    }

    protected void gui(boolean defaultVisibility) {
        firstFrameSetup(defaultVisibility);
        pushStyle();
        colorMode(HSB, 1, 1, 1, 1);
        resetMatrixInAnyRenderer();
        updateTrayBackground();
        updateSpecialButtons();
        updateGroupsAndTheirElements();
        updateFps();
        if (overlayVisible) {
            overlayOwner.updateOverlay();
        }
        popStyle();
        pMousePressed = mousePressed;
        keyboardAction = "";
    }

    private void firstFrameSetup(boolean defaultVisibility) {
        if (justStarted) {
            trayVisible = defaultVisibility;
            onPC = System.getProperty("os.name").toLowerCase().startsWith("windows");
            justStarted = false;
        }
    }

    private void updateFps() {
        if (!trayVisible) {
            return;
        }
        float x = cell * .25f;
        float y = height - cell;
        fill(fillText);
        textAlign(CENTER, CENTER);
        int nonFlickeringFrameRate = floor(frameRate > 55 ? 60 : frameRate);
        text(nonFlickeringFrameRate + " fps", x, y, cell * 2, cell);
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
        if (activated("hide/show", x, y, w, h)) {
            trayVisible = !trayVisible;
        }
        fill((keyboardSelected("hide/show") || isMouseOver(x, y, w, h)) ? fillTextSelected : fillText);
        text(trayVisible ? "hide" : "show", x, y, w, h);
    }

    private void updateSpecialUndoButton(float x, float y, float w, float h) {
        if (activated("undo", x, y, w, h)) {
            pushTopUndoOntoRedo();
            popUndo();
        }
        fill((keyboardSelected("undo") || isMouseOver(x, y, w, h)) ? fillTextSelected : fillText);
        text("undo", x, y, w, h);
    }

    private void updateSpecialRedoButton(float x, float y, float w, float h) {
        if (activated("redo", x, y, w, h)) {
            popRedo();
        }
        fill((keyboardSelected("redo") || isMouseOver(x, y, w, h)) ? fillTextSelected : fillText);
        text("redo", x, y, w, h);
    }

    private boolean activated(String query, float x, float y, float w, float h) {
        return mouseJustReleasedHere(x, y, w, h) || keyboardActivated(query);
    }

    private boolean keyboardActivated(String query) {
        return keyboardSelected(query) && keyboardAction.equals("ACTION");
    }

    private boolean keyboardSelected(String query) {
        if (!trayVisible) {
            keyboardSelectionIndex = 0;
        }
        if (!keyboardInputActive) {
            return false;
        }
        if ((query.equals("hide/show") && keyboardSelectionIndex == 0)
                || (query.equals("undo") && keyboardSelectionIndex == 1)
                || (query.equals("redo") && keyboardSelectionIndex == 2)) {
            return true;
        }
        int i = specialKeyCount;
        for (Group group : groups) {
            if (group.name.equals(query) && keyboardSelectionIndex == i) {
                return true;
            }
            i++;
            for (Element el : group.elements) {
                if ((group.name + el.name).equals(query) && keyboardSelectionIndex == i) {
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    int keyboardSelectableItemCount() {
        int elementCount = 0;
        for (Group group : groups) {
            elementCount += group.elements.size();
        }
        return specialKeyCount + groups.size() + elementCount;
    }

    private boolean mouseJustReleasedHere(float x, float y, float w, float h) {
        return pMousePressed && !mousePressed && isPointInRect(mouseX, mouseY, x, y, w, h);
    }

    private boolean isMouseOver(float x, float y, float w, float h) {
        return onPC && isPointInRect(mouseX, mouseY, x, y, w, h);
    }

    public void mousePressed() {
        keyboardInputActive = false;
    }

    public void keyPressed() {
        if (key == CODED) {
            if (keyCode == UP) {
                if (keyboardSelectionIndex == specialKeyCount) {
                    keyboardSelectionIndex = 0;
                } else {
                    keyboardSelectionIndex--;
                }
            } else if (keyCode == DOWN) {
                if (keyboardSelectionIndex < specialKeyCount) {
                    keyboardSelectionIndex = specialKeyCount;
                } else {
                    keyboardSelectionIndex++;
                }
            } else if (keyCode == LEFT) {
                if (keyboardSelectionIndex > 0 && keyboardSelectionIndex < specialKeyCount) {
                    keyboardSelectionIndex--;
                } else {
                    keyboardAction = "LEFT";
                }
            } else if (keyCode == RIGHT) {
                if (keyboardSelectionIndex < specialKeyCount) {
                    keyboardSelectionIndex++;
                } else {
                    keyboardAction = "RIGHT";
                }
            }
            keyboardSelectionIndex %= keyboardSelectableItemCount();
            if (keyboardSelectionIndex < 0) {
                keyboardSelectionIndex = keyboardSelectableItemCount() - 1;
            }
        }
        if (key == ' ' || key == ENTER) {
            keyboardAction = "ACTION";
        }
        keyboardInputActive = true;
    }

    protected void group(String name) {
        Group group = findGroup(name);
        if (!groupExists(name)) {
            group = new Group(name);
            groups.add(group);
        }
        setParentGroup(group);
    }

    private void updateGroupsAndTheirElements() {
        if (!trayVisible) {
            return;
        }
        float x = cell * .5f;
        float y = cell * 3;
        for (Group group : groups) {
            updateGroup(group, x, y);
            if (group.expanded) {
                x += cell * .5f;
                for (Element el : group.elements) {
                    y += cell;
                    updateElement(group, el, x, y);
                }
                x -= cell * .5f;
            }
            y += cell;
        }
    }

    private void updateGroup(Group group, float x, float y) {
        fill((keyboardSelected(group.name) || isMouseOver(0, y - cell, trayWidth, cell)) ? fillTextSelected : fillText);
        textAlign(LEFT, BOTTOM);
        textSize(fontSize);
        text(group.name, x, y);
        if (activated(group.name, 0, y - cell, trayWidth, cell)) {
            group.expanded = !group.expanded;
        }
    }

    private void updateElement(Group group, Element el, float x, float y) {
        fill((keyboardSelected(group.name + el.name) || isMouseOver(0, y - cell, trayWidth, cell)) ? fillTextSelected : fillText);
        textAlign(LEFT, BOTTOM);
        textSize(fontSize);
        text(el.name, x, y);
        if (activated(group.name + el.name, 0, y - cell, trayWidth, cell)) {
            if (!el.canHaveOverlay()) {
                println("cannot have overlay");
                el.handleActivation(0, y - cell, trayWidth, cell);
                return;
            }
            if (!overlayVisible) {
                overlayOwner = el;
                overlayVisible = true;
            }else if (overlayVisible && el.equals(overlayOwner)) {
                overlayVisible = false;
                pushUndo();
            }
        }
    }

    protected void updateTrayBackground() {
        if (!trayVisible) {
            return;
        }
        trayWidth = max(minimumTrayWidth, findLongestNameWidth() + cell * 2);
        noStroke();
        fill(0, backgroundAlpha);
        rectMode(CORNER);
        rect(0, 0, trayWidth, height);
        stroke(fillBackground, backgroundAlpha);
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
            parentGroup.elements.add(new SliderInt(name, defaultValue, defaultPrecision, getParentGroup()));
        }
        SliderInt slider = (SliderInt) findElementInGroup(name, parentGroup);
        return slider.value;
    }

    private void pushUndo() {

    }

    private void popUndo() {
    }

    private void pushTopUndoOntoRedo() {

    }

    private void popRedo() {

    }

    class Group {

        String name;
        boolean expanded = true;
        ArrayList<Element> elements = new ArrayList<>();

        public Group(String name) {
            this.name = name;
        }

        public Group(String name, boolean expanded) {
            this.name = name;
            this.expanded = expanded;
        }
    }

    abstract class Element {
        String name;

        abstract boolean canHaveOverlay();

        abstract void updateOverlay();

        abstract String value();

        public abstract String toString();

        public abstract void handleActivation(int x, float y, float w, float h);
    }

    class SliderInt extends Element {
        Group parent;
        int value, precision;

        public SliderInt(String name, int defaultValue, int defaultPrecision, Group parent) {
            this.parent = parent;
            this.name = name;
            this.value = defaultValue;
            this.precision = defaultPrecision;
        }

        @Override
        boolean canHaveOverlay() {
            return true;
        }

        @Override
        void updateOverlay() {
            noStroke();
            fill(1);
            rect(0, height - cell * 8, width, cell * 8);
        }

        @Override
        String value() {
            return String.valueOf(value);
        }

        @Override
        public String toString() {
            return parent.name + " " + name + ": " + value();
        }

        @Override
        public void handleActivation(int x, float y, float w, float h) {

        }
    }

    class SliderFloat extends Element {
        float value, precision;

        @Override
        boolean canHaveOverlay() {
            return true;
        }

        @Override
        void updateOverlay() {

        }

        @Override
        String value() {
            return null;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public void handleActivation(int x, float y, float w, float h) {

        }

    }

    class Slider2D extends Element {
        PVector value = new PVector();
        float precision;

        @Override
        boolean canHaveOverlay() {
            return true;
        }

        @Override
        void updateOverlay() {

        }

        @Override
        String value() {
            return null;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public void handleActivation(int x, float y, float w, float h) {

        }
    }

    class SliderColor extends Element {
        float hue, saturation, brightness;

        @Override
        boolean canHaveOverlay() {
            return true;
        }

        @Override
        void updateOverlay() {

        }

        @Override
        String value() {
            return null;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public void handleActivation(int x, float y, float w, float h) {

        }
    }

    class Radio extends Element {
        String[] options;

        @Override
        boolean canHaveOverlay() {
            return false;
        }

        @Override
        void updateOverlay() {

        }

        @Override
        String value() {
            return null;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public void handleActivation(int x, float y, float w, float h) {

        }
    }

    class Button extends Element {
        boolean justPressed = false;

        @Override
        boolean canHaveOverlay() {
            return false;
        }

        @Override
        void updateOverlay() {

        }

        @Override
        String value() {
            return null;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public void handleActivation(int x, float y, float w, float h) {

        }
    }

    class Toggle extends Element {
        boolean state = false;

        @Override
        boolean canHaveOverlay() {
            return false;
        }

        @Override
        void updateOverlay() {

        }

        @Override
        String value() {
            return null;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public void handleActivation(int x, float y, float w, float h) {

        }
    }
}
