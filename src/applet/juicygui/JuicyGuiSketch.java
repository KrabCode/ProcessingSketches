package applet.juicygui;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings({"InnerClassMayBeStatic", "SameParameterValue", "FieldCanBeLocal", "BooleanMethodIsAlwaysInverted", "unused"})
public class JuicyGuiSketch extends PApplet {
    //TODO FIX THE GROUP / NAME UNIQUENESS, test with fill

    // ----- Operation JUICE -----
    // gui elements:
    // gamepad support
    // mousewheel controls
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

    protected float textSize = 24;
    // color
    float backgroundAlpha = .5f;
    private boolean onPC;
    private ArrayList<Group> groups = new ArrayList<>();
    private Group parentGroup = null; //do not access directly!
    private float grayscaleBackground = .3f;
    private float grayscaleTextSelected = 1;
    private float grayscaleText = .6f;
    // input
    private boolean pMousePressed = false;
    private int keyboardSelectionIndex = 0;
    private String keyboardAction;
    private boolean keyboardInputActive = false;
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
        gui(true);
    }

    protected void gui(boolean defaultVisibility) {
        if (frameCount == 1) {
            textSize(textSize * 4);
            trayVisible = defaultVisibility;
            onPC = System.getProperty("os.name").toLowerCase().startsWith("windows");
            return;
        }
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
        if (!keyPressed || keyboardAction.equals("ACTION") || keyboardAction.equals("PAGE_DOWN") || keyboardAction.equals("PAGE_UP")) {
            keyboardAction = "";
        }
    }

    private void updateFps() {
        if (!trayVisible) {
            return;
        }
        fill(grayscaleText);
        textAlign(CENTER, CENTER);
        int nonFlickeringFrameRate = floor(frameRate > 55 ? 60 : frameRate);
        String text = nonFlickeringFrameRate + " fps";
        float x = width - textWidth(text) - cell * .5f;
        float y = 0;
        text(text, x, y, cell * 2, cell);
    }

    private void updateSpecialButtons() {
        float x = 0;
        float y = 0;
        textSize(textSize);
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
        fill((keyboardSelected("hide/show") || isMouseOver(x, y, w, h)) ? grayscaleTextSelected : grayscaleText);
        if (isMouseOver(x, y, w, h) || trayVisible) {
            text(trayVisible ? "hide" : "show", x, y, w, h);
        }
    }

    private void updateSpecialUndoButton(float x, float y, float w, float h) {
        if (activated("undo", x, y, w, h)) {
            pushTopUndoOntoRedo();
            popUndo();
        }
        fill((keyboardSelected("undo") || isMouseOver(x, y, w, h)) ? grayscaleTextSelected : grayscaleText);
        text("undo", x, y, w, h);
    }

    private void updateSpecialRedoButton(float x, float y, float w, float h) {
        if (activated("redo", x, y, w, h)) {
            popRedo();
        }
        fill((keyboardSelected("redo") || isMouseOver(x, y, w, h)) ? grayscaleTextSelected : grayscaleText);
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
        return onPC && (frameCount > 1) && isPointInRect(mouseX, mouseY, x, y, w, h);
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
                if (!overlayVisible && keyboardSelectionIndex > 0 && keyboardSelectionIndex < specialKeyCount) {
                    keyboardSelectionIndex--;
                }
                keyboardAction = "LEFT";
            } else if (keyCode == RIGHT) {
                if (!overlayVisible && keyboardSelectionIndex < specialKeyCount) {
                    keyboardSelectionIndex++;
                } else if (!overlayVisible && keyboardSelectionIndex > specialKeyCount) {
                    keyboardSelectionIndex = 1;
                }
                keyboardAction = "RIGHT";
            }

            keyboardSelectionIndex %= keyboardSelectableItemCount();
            if (keyboardSelectionIndex < 0) {
                keyboardSelectionIndex = keyboardSelectableItemCount() - 1;
            }
        }
        else if (key == '*') {
            keyboardAction = "PAGE_UP";
        } else if (key == '/') {
            keyboardAction = "PAGE_DOWN";
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
        fill((keyboardSelected(group.name) || isMouseOver(0, y - cell, trayWidth, cell)) ? grayscaleTextSelected : grayscaleText);
        textAlign(LEFT, BOTTOM);
        textSize(textSize);
        text(group.name, x, y);
        if (activated(group.name, 0, y - cell, trayWidth, cell)) {
            group.expanded = !group.expanded;
        }
    }

    private void updateElement(Group group, Element el, float x, float y) {
        fill((keyboardSelected(group.name + el.name) || isMouseOver(0, y - cell, trayWidth, cell)) ? grayscaleTextSelected : grayscaleText);
        el.displayOnTray(x, y);
        el.update();
        if (activated(group.name + el.name, 0, y - cell, trayWidth, cell)) {
            if (!el.canHaveOverlay()) {
                el.onActivationWithoutOverlay(0, y - cell, trayWidth, cell);
                return;
            }
            if (!overlayVisible) {
                overlayOwner = el;
                el.onOverlayShown();
                overlayVisible = true;
            } else if (overlayVisible && !el.equals(overlayOwner)) {
                overlayOwner.onOverlayHidden();
                overlayOwner = el;
                el.onOverlayShown();
            } else if (overlayVisible && el.equals(overlayOwner)) {
                overlayOwner.onOverlayHidden();
                overlayVisible = false;
                pushUndo();
            }

        }
    }

    protected void updateTrayBackground() {
        if (!trayVisible) {
            return;
        }
        textSize(textSize);
        trayWidth = max(minimumTrayWidth, findLongestNameWidth() + cell * 2);
        noStroke();
        fill(0, backgroundAlpha);
        rectMode(CORNER);
        rect(0, 0, trayWidth, height);
        stroke(grayscaleBackground, backgroundAlpha);
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
                if (el.trayTextWidth() > longestNameWidth) {
                    longestNameWidth = el.trayTextWidth();
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

    private void pushUndo() {

    }

    private void popUndo() {
    }

    private void pushTopUndoOntoRedo() {

    }

    private void popRedo() {

    }

    protected float sliderFloat(String name, float defaultValue, float precision) {
        if (!elementExists(name, getParentGroup().name)) {
            SliderFloat newElement = new SliderFloat(getParentGroup(), name, defaultValue, precision);
            newElement.update();
            getParentGroup().elements.add(newElement);
        }
        SliderFloat slider = (SliderFloat) findElement(name, getParentGroup().name);
        return slider.value;
    }

    protected PVector slider2D(String name, float defaultX, float defaultY, float precision) {
        if (!elementExists(name, getParentGroup().name)) {
            Slider2D newElement = new Slider2D(getParentGroup(), name, defaultX, defaultY, precision);
            newElement.update();
            getParentGroup().elements.add(newElement);
        }
        Slider2D slider = (Slider2D) findElement(name, getParentGroup().name);
        return slider.value;
    }

    protected int sliderColor(String name, float hue, float sat, float br) {
        if (!elementExists(name, getParentGroup().name)) {
            SliderColor newElement = new SliderColor(getParentGroup(), name, hue, sat, br);
            newElement.update();
            getParentGroup().elements.add(newElement);
        }
        SliderColor slider = (SliderColor) findElement(name, getParentGroup().name);
        return slider.value;
    }

    protected String radio(String defaultValue, String... otherValues) {
        if (!elementExists(defaultValue, getParentGroup().name)) {
            Element newElement = new Radio(getParentGroup(), defaultValue, otherValues);
            newElement.update();
            getParentGroup().elements.add(newElement);
        }
        Radio radio = (Radio) findElement(defaultValue, getParentGroup().name);
        return radio.options.get(radio.valueIndex);
    }

    protected boolean button(String name) {
        if (!elementExists(name, getParentGroup().name)) {
            Button newElement = new Button(getParentGroup(), name);
            newElement.update();
            getParentGroup().elements.add(newElement);
        }
        Button radio = (Button) findElement(name, getParentGroup().name);
        return radio.value;
    }

    private boolean elementExists(String elementName, String groupName) {
        return findElement(elementName, groupName) != null;
    }

    private Element findElement(String elementName, String groupName) {
        for (Group g : groups) {
            for (Element el : g.elements) {
                if (g.name.equals(groupName) && el.name.equals(elementName)) {
                    return el;
                }
            }
        }
        return null;
    }

    class Group {
        String name;
        boolean expanded = true;
        ArrayList<Element> elements = new ArrayList<>();

        public Group(String name) {
            this.name = name;
        }
    }

    abstract class Element {
        Group parent;
        String name;

        public Element(Group parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        abstract boolean canHaveOverlay();

        void update() {

        }

        void updateOverlay() {

        }

        abstract String valueForConsole();

        public String toString() {
            return parent + " " + name + " - " + valueForConsole();
        }

        public void onActivationWithoutOverlay(int x, float y, float w, float h) {

        }

        public void displayOnTray(float x, float y) {
            textAlign(LEFT, BOTTOM);
            textSize(textSize);
            text(name, x, y);
        }

        public float trayTextWidth() {
            return textWidth(name);
        }

        public void onOverlayShown() {

        }

        public void onOverlayHidden() {

        }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    abstract class Slider extends Element {
        float speed = 0;
        float drag = .6f;

        public Slider(Group parent, String name) {
            super(parent, name);
        }

        protected float updateInfiniteSlider(float precision, float sliderWidth) {
            if (mousePressed && !isPointInRect(mouseX, mouseY, 0, 0, trayWidth, height)) {
                float screenSpaceDelta = pmouseX - mouseX;
                float valueToScreenRatio = (precision * 2) / sliderWidth;
                float valueSpaceDelta = screenSpaceDelta * valueToScreenRatio;
                if (abs(valueSpaceDelta) > 0) {
                    return valueSpaceDelta;
                }
            }
            if (keyboardAction.equals("LEFT")) {
                return -5;
            } else if (keyboardAction.equals("RIGHT")) {
                return 5;
            }
            return 0;
        }

        protected void infiniteSliderCenterMode(float x, float y, float w, float h, float precision, float value) {
            pushMatrix();
            pushStyle();
            translate(x, y);
            strokeWeight(2);
            drawHorizontalLine(-w, w);
            drawMarkerLines(precision * 0.5f, h * .8f, true, value, precision, w);
            drawMarkerLines(precision * 0.25f, h * .5f, true, value, precision, w);
            drawMarkerLines(precision * .025f, h * .2f, false, value, precision, w);
            strokeWeight(1);
            drawSelectionBox(h, precision, value);
            popMatrix();
            popStyle();
        }

        private void drawHorizontalLine(float leftEdgeX, float rightEdgeX) {
            stroke(grayscaleText);
            line(leftEdgeX, 0, rightEdgeX, 0);
        }

        private void drawSelectionBox(float sliderHeight, float precision, float value) {
            stroke(grayscaleText);
            noFill();
            rectMode(CENTER);
            rect(0, 0, 20, sliderHeight, 20);
            fill(grayscaleText);
            float textY = -cell * 2;
            if (abs(value) < 1) {
                text(String.valueOf(value), 0, textY);
            } else {
                text(nf(value, 0, 0), 0, textY);
            }
        }

        private void drawMarkerLines(float frequency, float markerHeight, boolean shouldDrawValue, float value, float precision, float w) {
            float valueOnLeftEdge = -precision;
            float valueOnRightEdge = precision;
            float markerValue = valueOnLeftEdge - value;
            while (markerValue <= valueOnRightEdge - value) {
                float moduloValue = markerValue;
                //TODO remove the silly while and replace with multiplication for a tiny speed boost and hang prevention
                while (moduloValue > valueOnRightEdge) {
                    moduloValue -= precision * 2;
                }
                while (moduloValue < valueOnLeftEdge) {
                    moduloValue += precision * 2;
                }
                float screenX = map(moduloValue, valueOnLeftEdge, valueOnRightEdge, -w, w);
                stroke(grayscaleText);
                line(screenX, -markerHeight * .5f, screenX, markerHeight * .5f);
                if (shouldDrawValue) {
                    float displayValue = moduloValue + value;
                    if(abs(displayValue) > 50){
                        displayValue = floor(displayValue);
                    }
                    String displayText = nf(displayValue, 0, 0);
                    if (displayText.equals("-0")) {
                        displayText = "0";
                    }
                    fill(grayscaleText);
                    textAlign(CENTER, CENTER);
                    textSize(textSize);
                    text(displayText, screenX, cell);
                }
                markerValue += frequency;
            }
        }
    }

    class SliderFloat extends Slider {
        float value, precision;

        public SliderFloat(Group parent, String name, float defaultValue, float precision) {
            super(parent, name);
            this.value = defaultValue;
            this.precision = precision;
        }

        boolean canHaveOverlay() {
            return true;
        }

        void updateOverlay() {
            //todo
            // if name contains angle: draw a circle instead

            float sliderHeight = cell * 2;
            fill(grayscaleText);
            float acc = updateInfiniteSlider(precision, width);
            if (keyboardAction.equals("PAGE_UP")) {
                precision *= 10;
            } else if (keyboardAction.equals("PAGE_DOWN")) {
                precision *= .1f;
            }
            precision = max(1, precision);
            if (abs(acc) > 0) {
                speed = acc;
            }
            speed *= drag;
            value += speed;
            infiniteSliderCenterMode(width * .5f, height - sliderHeight, width, sliderHeight, precision, value);
        }

        String valueForConsole() {
            return nf(value, 0, 0);
        }
    }

    class Slider2D extends Slider {
        PVector value = new PVector();
        float precision;

        public Slider2D(Group parent, String name, float defaultX, float defaultY, float precision) {
            super(parent, name);
            this.value.x = defaultX;
            this.value.y = defaultY;
            this.precision = precision;
        }

        boolean canHaveOverlay() {
            return true;
        }

        void updateOverlay() {
            //todo sick orthogonal slider action
        }

        String valueForConsole() {
            return String.valueOf(value);
        }
    }

    class SliderColor extends Slider {
        float hue, saturation, brightness;
        int value;

        public SliderColor(Group parent, String name, float hue, float sat, float br) {
            super(parent, name);
            this.hue = hue;
            this.saturation = sat;
            this.brightness = br;
        }

        boolean canHaveOverlay() {
            return true;
        }

        void updateOverlay() {
            // TODO draw fancy stuff
        }

        void update() {
            pushStyle();
            colorMode(HSB, 1, 1, 1, 1);
            value = color(hue, saturation, brightness);
            popStyle();
        }

        String valueForConsole() {
            return "h " + hue + " s " + saturation + " b " + brightness;
        }
    }

    class Radio extends Element {
        int valueIndex = 0;
        ArrayList<String> options = new ArrayList<>();

        public Radio(Group parent, String name, String[] options) {
            super(parent, name);
            this.options.add(name);
            this.options.addAll(Arrays.asList(options));
        }

        boolean canHaveOverlay() {
            return false;
        }

        String valueForConsole() {
            return options.get(valueIndex);
        }

        String value() {
            return options.get(valueIndex);
        }

        @Override
        public void displayOnTray(float x, float y) {
            textAlign(LEFT, BOTTOM);
            textSize(textSize);
            pushStyle();
            float optionX = x;
            for (int i = 0; i < options.size(); i++) {
                String option = options.get(i);
                if (i < options.size() - 1) {
                    option += ", ";
                }
                fill(i == valueIndex ? grayscaleTextSelected : grayscaleText);
                text(option, optionX, y);
                optionX += textWidth(option);
            }
            popStyle();
        }


        public void onActivationWithoutOverlay(int x, float y, float w, float h) {
            valueIndex++;
            if (valueIndex >= options.size()) {
                valueIndex = 0;
            }
        }
    }

    class Button extends Element {
        public boolean value;
        boolean justPressed = false;

        public Button(Group parent, String name) {
            super(parent, name);
        }

        boolean canHaveOverlay() {
            return false;
        }

        String valueForConsole() {
            return String.valueOf(value);
        }

        public void onActivationWithoutOverlay(int x, float y, float w, float h) {
            justPressed = true;
        }

        public void displayOnTray(float x, float y) {
            textAlign(LEFT, BOTTOM);
            textSize(textSize);
            if (justPressed) {
                fill(grayscaleTextSelected);
            }
            text(name, x, y);
        }

        void update() {
            justPressed = false;
        }
    }

    class Toggle extends Element {
        boolean state = false;

        public Toggle(Group parent, String name) {
            super(parent, name);
        }

        boolean canHaveOverlay() {
            return false;
        }

        String valueForConsole() {
            return String.valueOf(state);
        }

        public void displayOnTray(float x, float y) {
            textAlign(LEFT, BOTTOM);
            textSize(textSize);
            if (state) {
                fill(grayscaleTextSelected);
            }
            text(name, x, y);
        }

        public void onActivationWithoutOverlay(int x, float y, float w, float h) {
            state = !state;
        }
    }
}
