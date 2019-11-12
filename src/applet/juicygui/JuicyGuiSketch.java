package applet.juicygui;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings({"InnerClassMayBeStatic", "SameParameterValue", "FieldCanBeLocal", "BooleanMethodIsAlwaysInverted", "unused", "ConstantConditions"})
public class JuicyGuiSketch extends PApplet {
    //TODO:
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

    private float textSize = 24;
    // color
    private float backgroundAlpha = .5f;
    private boolean onPC;
    private ArrayList<Group> groups = new ArrayList<Group>();
    private Group parentGroup = null; // do not access directly!
    private float grayscaleGrid = .3f;
    private float grayscaleTextSelected = 1;
    private float grayscaleText = .6f;
    // input
    private boolean pMousePressed = false;
    private int keyboardSelectionIndex = 0;
    private String keyboardAction = "";
    private boolean keyboardInputActive = false;
    private int specialButtonCount = 3;

    private ArrayList<Key> keyboardKeys = new ArrayList<Key>();
    private ArrayList<Key> keyboardKeysToRemove = new ArrayList<Key>();
    private int keyRepeatDelayFirst = 200;
    private int keyRepeatDelay = 30;


    // layout
    private boolean trayVisible = true;
    private float cell = 40;
    private float minimumTrayWidth = cell * 6;
    private float trayWidth;

    //overlay
    private boolean overlayVisible;
    private Element overlayOwner; // do not assign directly!

    private void pushUndo() {

    }

    private void popUndo() {
    }

    private void pushTopUndoOntoRedo() {

    }

    private void popRedo() {

    }


    protected void gui() {
        gui(true);
    }

    protected void gui(boolean defaultVisibility) {
        if (frameCount == 1) {
            //the maximum text size we want to ever use needs to be called first, otherwise the font is stretched and ugly
            textSize(textSize * 2);
            trayVisible = defaultVisibility;
            onPC = System.getProperty("os.name").toLowerCase().startsWith("windows");
            return;
        }

        handleKeyboardInput();
        pushStyle();
        strokeCap(SQUARE);
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
        if (!keyPressed || (!keyboardAction.equals("") &&
                !keyboardAction.equals("LEFT") &&
                !keyboardAction.equals("RIGHT"))
        ) {
            keyboardAction = "";
        }
    }

    private void updateFps() {
        if (!trayVisible) {
            return;
        }
        textAlign(CENTER, CENTER);
        int nonFlickeringFrameRate = floor(frameRate > 55 ? 60 : frameRate);
        String text = nonFlickeringFrameRate + " fps";
        float x = width - cell;
        float y = textSize;
        rectMode(CENTER);
        noStroke();
        fill(0, backgroundAlpha);
        rect(x, y, cell * 2, cell);
        fill(grayscaleText);
        text(text, x, y);
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

    private void setOverlayOwner(Element overlayOwner) {
        if (this.overlayOwner != null) {
            this.overlayOwner.onOverlayHidden();
        }
        this.overlayOwner = overlayOwner;
        this.overlayOwner.onOverlayShown();
    }

    // INPUT

    private boolean activated(String query, float x, float y, float w, float h) {
        return mouseJustReleasedHere(x, y, w, h) || keyboardActivated(query);
    }

    private boolean keyboardActivated(String query) {
        return keyboardSelected(query) && keyboardAction.equals("ACTION");
    }

    private boolean isAnyGroupKeyboardSelected() {
        return findKeyboardSelectedGroup() != null;
    }

    private boolean isAnyElementKeyboardSelected() {
        return findKeyboardSelectedElement() != null;
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
        int i = specialButtonCount;
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

    private int keyboardSelectableItemCount() {
        int elementCount = 0;
        for (Group group : groups) {
            elementCount += group.elements.size();
        }
        return specialButtonCount + groups.size() + elementCount;
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
        keyboardInputActive = true;
        if (key == CODED) {
            if(!isPressed(keyCode, true)){
                keyboardKeys.add(new Key(keyCode, true));
            }
        } else {
            if(!isPressed(key, false)){
                keyboardKeys.add(new Key((int) key, false));
            }
        }
        println(keyboardKeys.size());
    }

    private boolean isPressed(int keyCode, boolean coded) {
        for(Key kk : keyboardKeys){
            if(kk.character == keyCode && kk.coded == coded){
                return true;
            }
        }
        return false;
    }

    public void keyReleased() {
        if (key == CODED) {
            removeKey(keyCode, true);
        } else {
            removeKey(key, false);
        }
    }

    private void removeKey(int keyCodeToRemove, boolean coded) {
        keyboardKeysToRemove.clear();
        for (Key kk : keyboardKeys) {
            if (kk.coded == coded && kk.character == keyCodeToRemove) {
                keyboardKeysToRemove.add(kk);
            }
        }
        keyboardKeys.removeAll(keyboardKeysToRemove);
    }

    class Key {
        boolean justPressed = true;
        boolean repeatedAlready = false;
        boolean coded;
        int character;
        int lastRegistered = -1;

        Key(Integer character, boolean coded) {
            this.character = character;
            this.coded = coded;
        }


    }

    private void handleKeyboardInput() {
        for (Key kk : keyboardKeys) {
            if (kk.coded) {
                if (kk.justPressed ||
                        (!kk.repeatedAlready && millis() > kk.lastRegistered + keyRepeatDelayFirst) ||
                        ( kk.repeatedAlready && millis() > kk.lastRegistered + keyRepeatDelay)) {
                    kk.lastRegistered = millis();
                    if(!kk.justPressed){
                        kk.repeatedAlready = true;
                    }
                    if (kk.character == UP) {
                        if (keyboardSelectionIndex == specialButtonCount) {
                            keyboardSelectionIndex = 0;
                        } else {
                            keyboardSelectionIndex -= hiddenElementCount(false);
                            keyboardSelectionIndex--;
                        }
                        if (isAnyElementKeyboardSelected()) {
                            setOverlayOwner(findKeyboardSelectedElement());
                        }
                    }
                    if (kk.character == DOWN) {
                        if (keyboardSelectionIndex < specialButtonCount) {
                            keyboardSelectionIndex = specialButtonCount;
                        } else {
                            keyboardSelectionIndex += hiddenElementCount(true);
                            keyboardSelectionIndex++;
                        }
                        if (isAnyElementKeyboardSelected()) {
                            setOverlayOwner(findKeyboardSelectedElement());
                        }
                    }
                }
                if (kk.character == LEFT) {
                    if (!overlayVisible && keyboardSelectionIndex < specialButtonCount) {
                        keyboardSelectionIndex--;
                    }
                    if (isAnyGroupKeyboardSelected() && findKeyboardSelectedGroup().expanded) {
                        findKeyboardSelectedGroup().expanded = !findKeyboardSelectedGroup().expanded;
                    } else {
                        keyboardAction = "LEFT";
                    }
                }
                if (kk.character == RIGHT) {
                    if (!overlayVisible && keyboardSelectionIndex < specialButtonCount) {
                        keyboardSelectionIndex++;
                    }
                    if (isAnyGroupKeyboardSelected() && !findKeyboardSelectedGroup().expanded) {
                        findKeyboardSelectedGroup().expanded = !findKeyboardSelectedGroup().expanded;
                    } else {
                        keyboardAction = "RIGHT";
                    }
                }
            } else {
                if(!kk.justPressed){
                    continue;
                }
                if (kk.character == (int)'*' || kk.character == (int)'+') {
                    keyboardAction = "PRECISION_UP";
                }
                if (kk.character == '/' || kk.character == '-') {
                    keyboardAction = "PRECISION_DOWN";
                }
                if (kk.character == ' ' || kk.character == ENTER) {
                    keyboardAction = "ACTION";
                }
                if (kk.character == 'r') {
                    keyboardAction = "RESET";
                }
            }
            kk.justPressed = false;
        }
        keyboardSelectionIndex %= keyboardSelectableItemCount();
        if (keyboardSelectionIndex < 0) {
            Group lastGroup = getLastGroup();
            if (!lastGroup.expanded) {
                keyboardSelectionIndex = keyboardSelectableItemCount() - lastGroup.elements.size() - 1;
            } else {
                keyboardSelectionIndex = keyboardSelectableItemCount() - 1;
            }
        }
    }

    public void mouseWheel(MouseEvent event) {
        float direction = event.getCount();
        if (direction > 0) {
            keyboardAction = "TINY_RIGHT";
        } else if (direction < 0) {
            keyboardAction = "TINY_LEFT";
        }
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
        float grayScale = keyboardSelected(group.name + el.name) || isMouseOver(0, y - cell, trayWidth, cell) ? grayscaleTextSelected : grayscaleText;
        fill(grayScale);
        stroke(grayScale);
        el.displayOnTray(x, y);
        el.update();
        if (activated(group.name + el.name, 0, y - cell, trayWidth, cell)) {
            if (!el.canHaveOverlay()) {
                el.onActivationWithoutOverlay(0, y - cell, trayWidth, cell);
                return;
            }
            if (!overlayVisible) {
                setOverlayOwner(el);
                overlayVisible = true;
            } else if (overlayVisible && !el.equals(overlayOwner)) {
                setOverlayOwner(el);
            } else if (overlayVisible && el.equals(overlayOwner)) {
                overlayOwner.onOverlayHidden();
                overlayVisible = false;
                pushUndo();
            }

        }
    }

    private void updateTrayBackground() {
        if (!trayVisible) {
            return;
        }
        textSize(textSize);
        trayWidth = max(minimumTrayWidth, findLongestNameWidth() + cell * 2);
        noStroke();
        fill(0, backgroundAlpha);
        rectMode(CORNER);
        rect(0, 0, trayWidth, height);
//        drawTrayGrid();
    }

    private void drawTrayGrid() {
        stroke(grayscaleGrid);
        for (float x = cell; x < trayWidth; x += cell) {
            line(x, 0, x, height);
        }
        for (float y = cell; y < height; y += cell) {
            line(0, y, trayWidth, y);
        }
    }

    private void resetMatrixInAnyRenderer() {
        if (sketchRenderer().equals(P3D)) {
            camera();
        } else {
            resetMatrix();
        }
    }

    // GROUP / ELEMENT HANDLING

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

    private int hiddenElementCount(boolean forwardFacing) {
        if (isAnyGroupKeyboardSelected()) {
            Group group;
            if (forwardFacing) {
                group = findKeyboardSelectedGroup();
            } else {
                group = getPreviousGroup(findKeyboardSelectedGroup().name);
            }
            if (!group.expanded) {
                return group.elements.size();
            }
        }
        return 0;
    }

    private Group getParentGroup() {
        if (parentGroup == null) {
            Group anonymous = new Group("");
            groups.add(anonymous);
            parentGroup = anonymous;
        }
        return parentGroup;
    }

    private Group getLastGroup() {
        return groups.get(groups.size() - 1);
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

    private Group getPreviousGroup(String query) {
        for (Group group : groups) {
            if (group.name.equals(query)) {
                int index = groups.indexOf(group);
                if (index > 0) {
                    return groups.get(index - 1);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private Group getNextGroup(String query) {
        for (Group group : groups) {
            if (group.name.equals(query)) {
                int index = groups.indexOf(group);
                if (index < groups.size() - 1) {
                    return groups.get(index + 1);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private Group findKeyboardSelectedGroup() {
        for (Group group : groups) {
            if (keyboardSelected(group.name)) {
                return group;
            }
        }
        return null;
    }

    private Element findKeyboardSelectedElement() {
        for (Group group : groups) {
            for (Element el : group.elements)
                if (keyboardSelected(group.name + el.name)) {
                    return el;
                }
        }
        return null;
    }

    protected float sliderFloat(String name, float defaultValue, float precision) {
        if (!elementExists(name, getParentGroup().name)) {
            SliderFloat newElement = new SliderFloat(getParentGroup(), name, defaultValue, precision);
            getParentGroup().elements.add(newElement);
        }
        SliderFloat slider = (SliderFloat) findElement(name, getParentGroup().name);
        assert slider != null;
        return slider.value;
    }

    protected PVector slider2D(String name, float defaultX, float defaultY, float precision) {
        if (!elementExists(name, getParentGroup().name)) {
            Slider2D newElement = new Slider2D(getParentGroup(), name, defaultX, defaultY, precision);
            getParentGroup().elements.add(newElement);
        }
        Slider2D slider = (Slider2D) findElement(name, getParentGroup().name);
        assert slider != null;
        return slider.value;
    }

    protected int sliderColor(String name, float hue, float sat, float br) {
        if (!elementExists(name, getParentGroup().name)) {
            SliderColor newElement = new SliderColor(getParentGroup(), name, hue, sat, br);
            getParentGroup().elements.add(newElement);
        }
        SliderColor slider = (SliderColor) findElement(name, getParentGroup().name);
        assert slider != null;
        return slider.value;
    }

    protected String radio(String defaultValue, String... otherValues) {
        if (!elementExists(defaultValue, getParentGroup().name)) {
            Element newElement = new Radio(getParentGroup(), defaultValue, otherValues);
            getParentGroup().elements.add(newElement);
        }
        Radio radio = (Radio) findElement(defaultValue, getParentGroup().name);
        assert radio != null;
        return radio.options.get(radio.valueIndex);
    }

    protected boolean button(String name) {
        if (!elementExists(name, getParentGroup().name)) {
            Button newElement = new Button(getParentGroup(), name);
            getParentGroup().elements.add(newElement);
        }
        Button button = (Button) findElement(name, getParentGroup().name);
        return button.value;
    }

    protected boolean toggle(String name) {
        return toggle(name, false);
    }

    protected boolean toggle(String name, boolean defaultState) {
        if (!elementExists(name, getParentGroup().name)) {
            Toggle newElement = new Toggle(getParentGroup(), name, defaultState);
            getParentGroup().elements.add(newElement);
        }
        Toggle toggle = (Toggle) findElement(name, getParentGroup().name);
        return toggle.state;
    }

    private boolean elementExists(String elementName, String groupName) {
        return findElement(elementName, groupName) != null;
    }

    private boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
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
        ArrayList<Element> elements = new ArrayList<Element>();

        Group(String name) {
            this.name = name;
        }
    }

    abstract class Element {
        Group parent;
        String name;

        Element(Group parent, String name) {
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
            if (this.equals(overlayOwner)) {
                strokeWeight(1);
                stroke(grayscaleText);
                line(x, y, x + textWidth(name), y);
            }
            text(name, x, y);
        }

        float trayTextWidth() {
            return textWidth(name);
        }

        void onOverlayShown() {

        }

        void onOverlayHidden() {

        }
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "DuplicatedCode"})
    abstract class Slider extends Element {
        float speed = 0;
        float drag = .5f;

        Slider(Group parent, String name) {
            super(parent, name);
        }

        float updateInfiniteSlider(float precision, float sliderWidth) {
            if (mousePressed && !isPointInRect(mouseX, mouseY, 0, 0, trayWidth, height)) {
                float screenSpaceDelta = pmouseX - mouseX;
                float valueSpaceDelta = screenDistanceToValueDistance(screenSpaceDelta, precision, sliderWidth);
                if (abs(valueSpaceDelta) > 0) {
                    return valueSpaceDelta;
                }
            }
            if (keyboardAction.equals("LEFT")) {
                return screenDistanceToValueDistance(-3, precision, sliderWidth);
            } else if (keyboardAction.equals("RIGHT")) {
                return screenDistanceToValueDistance(3, precision, sliderWidth);
            } else if (keyboardAction.equals("TINY_LEFT")) {
                return screenDistanceToValueDistance(-1, precision, sliderWidth);
            } else if (keyboardAction.equals("TINY_RIGHT")) {
                return screenDistanceToValueDistance(1, precision, sliderWidth);
            }
            return 0;
        }

        private float screenDistanceToValueDistance(float screenSpaceDelta, float precision, float sliderWidth) {
            float valueToScreenRatio = precision / sliderWidth;
            return screenSpaceDelta * valueToScreenRatio;
        }

        void drawInfiniteSliderCenterMode(float x, float y, float w, float h, float precision, float value) {
            pushMatrix();
            pushStyle();
            translate(x, y);
            noStroke();
            fill(0, backgroundAlpha);
            rectMode(CENTER);
            rect(0, 0, w, h);
            strokeWeight(3);
            drawHorizontalLine(-w, w);
            drawMarkerLines(precision * 0.25f, h * .6f, true, value, precision, w);
            drawMarkerLines(precision * .025f, h * .3f, false, value, precision, w);
            drawValue(h, precision, value);
            popMatrix();
            popStyle();
        }

        private void drawHorizontalLine(float leftEdgeX, float rightEdgeX) {
            stroke(grayscaleText);
            line(leftEdgeX, 0, rightEdgeX, 0);
        }

        private void drawValue(float sliderHeight, float precision, float value) {
            fill(grayscaleText);
            textAlign(CENTER, CENTER);
            textSize(textSize * 2);
            float textY = -cell * 3;
            String text = nf(value, 0, 0);
            noStroke();
            fill(0, .5f);
            rectMode(CENTER);
            rect(0, textY + textSize * .33f, textWidth(text) + 20, textSize * 2 + 20);
            fill(grayscaleTextSelected);
            text(text, 0, textY);
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
                line(screenX, -markerHeight * .5f, screenX, 0);
                if (shouldDrawValue) {
                    float displayValue = moduloValue + value;
                    if (abs(displayValue) > 10) {
                        displayValue = floor(moduloValue) + ceil(value);
                    }
                    String displayText = nf(displayValue, 0, 0);
                    if (displayText.equals("-0")) {
                        displayText = "0";
                    }
                    pushMatrix();
                    fill(grayscaleText);
                    textAlign(CENTER, TOP);
                    textSize(textSize);
                    text(displayText, screenX + ((displayText.equals("0") || displayValue > 0) ? 0 : -textWidth("-") * .5f), 0);
                    popMatrix();
                }
                markerValue += frequency;
            }
        }
    }

    class SliderFloat extends Slider {
        float value, precision, defaultValue, defaultPrecision, minValue, maxValue;
        boolean constrained = false;

        SliderFloat(Group parent, String name, float defaultValue, float precision) {
            super(parent, name);
            this.value = defaultValue;
            this.defaultValue = defaultValue;
            this.precision = precision;
            this.defaultPrecision = precision;
            if (name.equals("fill") || name.equals("stroke")) {
                constrained = true;
                minValue = 0;
                maxValue = 255;
            }
        }

        boolean canHaveOverlay() {
            return true;
        }

        void updateOverlay() {
            //todo
            // if name or limit is an angle: draw a circle instead

            float sliderHeight = cell * 2;
            float valueDelta = updateInfiniteSlider(precision, width);
            if (keyboardAction.equals("PRECISION_UP") && precision < 10000) {
                precision *= 10;
            } else if (keyboardAction.equals("PRECISION_DOWN") && precision > 1) {
                precision *= .1f;
            } else if (keyboardAction.equals("RESET")) {
                precision = defaultPrecision;
                value = defaultValue;
            }
            value += valueDelta;
            if (constrained) {
                value = constrain(value, minValue, maxValue);
            }
            drawInfiniteSliderCenterMode(width * .5f, height - cell, width, sliderHeight, precision, value);
        }

        String valueForConsole() {
            return nf(value, 0, 0);
        }
    }

    class Slider2D extends Slider {
        PVector value = new PVector();
        float precision;

        Slider2D(Group parent, String name, float defaultX, float defaultY, float precision) {
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

        SliderColor(Group parent, String name, float hue, float sat, float br) {
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
        ArrayList<String> options = new ArrayList<String>();

        Radio(Group parent, String name, String[] options) {
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
                float textWidthWithoutSeparator = textWidth(option);
                if (i < options.size() - 1) {
                    option += " ";
                }
                if (i == valueIndex) {
                    strokeWeight(2);
                    line(optionX, y, optionX + textWidthWithoutSeparator, y);
                }
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

        Button(Group parent, String name) {
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
        boolean state, defaultState;

        Toggle(Group parent, String name, boolean defaultState) {
            super(parent, name);
            this.defaultState = defaultState;
            this.state = defaultState;
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
                strokeWeight(2);
                line(x, y, x + textWidth(name), y);
            }
            text(name, x, y);
        }

        void update() {
            if (keyboardAction.equals("RESET")) {
                state = defaultState;
            }
        }

        public void onActivationWithoutOverlay(int x, float y, float w, float h) {
            state = !state;
        }
    }
}
