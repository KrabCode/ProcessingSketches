package applet;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

//TODO:
// features:
// remove undo / redo stack size number, but communicate emptiness somehow

// menu buttons: save and load state, hold undo/redo to undo/redo all

// tray hold dropdown:
// touchscreen slider precision, reset
// options dropdown instead of horizontal enumeration for tray width reasons

// record states with frame stamp before recording in realtime or well controlled slow-mo, then play them back
// intercept esc / exit and save state first?
// pushGroup(name), popGroup();
// scrolling down to allow unlimited group and element count
// more minimalist animated juice...
// range picker (2 floats, start and end, end > start)
// out of constrain markers must not be displayed

//TODO
// bugs:
// space when tray hidden must close current overlay
// first null group's element appears twice, once at the top and once at the bottom


@SuppressWarnings({"InnerClassMayBeStatic", "SameParameterValue", "FieldCanBeLocal",
        "BooleanMethodIsAlwaysInverted", "unused", "ConstantConditions", "WeakerAccess"})
public abstract class NewGuiSketch extends PApplet {
    private static final String SAVE_PREFIX = "SAVE";
    private static final String STATE_BEGIN = "STATE_BEGIN";
    private static final String STATE_END = "STATE_END";
    private static final String INNER_SEPARATOR = "§";
    private static final String UNDO_PREFIX = "UNDO";
    private static final String REDO_PREFIX = "REDO";
    private static final String GROUP_PREFIX = "GROUP";
    private static final String ACTION_UP = "UP";
    private static final String ACTION_DOWN = "DOWN";
    private static final String ACTION_LEFT = "LEFT";
    private static final String ACTION_RIGHT = "RIGHT";
    private static final String ACTION_PRECISION_ZOOM_IN = "PRECISION_ZOOM_IN";
    private static final String ACTION_PRECISION_ZOOM_OUT = "PRECISION_ZOOM_OUT";
    private static final String ACTION_RESET = "RESET";
    private static final String ACTION_CONTROL = "CONTROL";
    private static final String ACTION_ALT = "ALT";
    private static final String ACTION_ACTIVATE = "ACTIVATE";
    private static final String ACTION_UNDO = "UNDO";
    private static final String ACTION_REDO = "REDO";
    private static final String MENU_BUTTON_REDO = "redo";
    private static final String MENU_BUTTON_UNDO = "undo";
    private static final String MENU_BUTTON_HIDE = "hide";
    private static final String SATURATION = "saturation";
    private static final String BRIGHTNESS = "brightness";
    private static final String HUE = "hue";
    private static final float BACKGROUND_ALPHA = .9f;
    private static final float GRAYSCALE_GRID = .3f;
    private static final float GRAYSCALE_TEXT_DARK = .5f;
    private static final float GRAYSCALE_TEXT_SELECTED = 1;
    private static final float PRECISION_MAXIMUM = 10000;
    private static final float PRECISION_MINIMUM = .1f;
    private static final float ALPHA_PRECISION_MINIMUM = .005f;
    private static final float ALPHA_PRECISION_MAXIMUM = 10;
    private static final float UNDERLINE_TRAY_ANIMATION_DURATION = 10;
    private static final float UNDERLINE_TRAY_ANIMATION_EASING = 3;
    private static final float SLIDER_EDGE_DARKEN_EASING = 3;
    private static final float SLIDER_REVEAL_DURATION = 15;
    private static final float SLIDER_REVEAL_START_SKIP = SLIDER_REVEAL_DURATION * .25f;
    private static final float SLIDER_REVEAL_EASING = 1;
    private static final float PICKER_REVEAL_DURATION = 15;
    private static final float PICKER_REVEAL_EASING = 1;
    private static final float PICKER_REVEAL_START_SKIP = PICKER_REVEAL_DURATION * .25f;
    private static final int MENU_BUTTON_COUNT = 3;
    private static final int KEY_REPEAT_DELAY_FIRST = 300;
    private static final int KEY_REPEAT_DELAY = 30;
    private static final float MENU_ROTATION_DURATION = 10;
    private static final float MENU_ROTATION_EASING = .5f;
    private static final float DESELECTION_FADEOUT_DURATION = 10;
    private static final float DESELECTION_FADEOUT_EASING = 1;
    private static final float CHECK_ANIMATION_DURATION = 10;
    private static final float CHECK_ANIMATION_EASING = 1;
    protected String captureDir;
    protected String captureFilename;
    protected String id = regenId();
    protected int frameRecordingEnds;
    protected int recordingFrames = 360; // assuming t += radians(1) per frame for a perfect loop
    protected float t;
    protected boolean mousePressedOutsideGui = false;
    private ArrayList<ArrayList<String>> undoStack = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> redoStack = new ArrayList<ArrayList<String>>();
    private ArrayList<Group> groups = new ArrayList<Group>();
    private Group currentGroup = null; // do not assign to nor read directly!
    private ArrayList<Key> keyboardKeys = new ArrayList<Key>();
    private ArrayList<Key> keyboardKeysToRemove = new ArrayList<Key>();
    private ArrayList<String> actions = new ArrayList<String>();
    private ArrayList<String> previousActions = new ArrayList<String>();
    private boolean pMousePressed = false;
    private int keyboardSelectionIndex = MENU_BUTTON_COUNT;
    private boolean keyboardActive = true;
    private boolean onWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private float textSize = onWindows ? 24 : 48;
    private float cell = onWindows ? 40 : 80;
    private float minimumTrayWidthCellCount = 6;
    private float previewTrayBoxWidth = cell * .375f;
    private float previewTrayBoxMargin = cell * .125f;
    private float previewTrayBoxOffsetY = -cell * .025f;
    private float minimumTrayWidth = cell * minimumTrayWidthCellCount;
    private float trayWidth = minimumTrayWidth;
    private float sliderHeight = cell * 2;
    private boolean trayVisible = true;
    private boolean overlayVisible;
    private boolean horizontalOverlayVisible;
    private boolean verticalOverlayVisible;
    private boolean pickerOverlayVisible;
    private boolean zOverlayVisible;
    private Element overlayOwner = null; // do not assign directly!
    private float underlineTrayAnimationStarted = -UNDERLINE_TRAY_ANIMATION_DURATION;
    private float undoRotationStarted = -MENU_ROTATION_DURATION;
    private float redoRotationStarted = -MENU_ROTATION_DURATION;
    private float undoHideRotationStarted = -MENU_ROTATION_DURATION;

    // INTERFACE
    protected float slider() {
        return slider("x", 0);
    }

    protected float slider(String name) {
        return slider(name, 0);
    }

    protected float slider(String name, float defaultValue) {
        return slider(name, defaultValue, numberOfDigitsInFlooredNumber(defaultValue) * 10);
    }

    protected float slider(String name, float max, boolean floorOrCeil) {
        return slider(name, floorOrCeil ? 0 : max, max * .5f);
    }

    protected float slider(String name, float defaultValue, float precision) {
        return slider(name, defaultValue, precision, false, 0, 0);
    }

    protected float slider(String name, float min, float max, float defaultValue) {
        return slider(name, defaultValue, max - min, true, min, max);
    }

    protected float slider(String name, float defaultValue, float precision, boolean constrained, float min,
                           float max) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(name, currentGroup.name)) {
            SliderFloat newElement = new SliderFloat(currentGroup, name, defaultValue, precision,
                    constrained, min, max);
            currentGroup.elements.add(newElement);
        }
        SliderFloat slider = (SliderFloat) findElement(name, currentGroup.name);
        assert slider != null;
        return slider.value;
    }

    protected PVector sliderXY() {
        return sliderXY("xy");
    }

    protected PVector sliderXY(String name) {
        return sliderXY(name, 0, 0, 100);
    }

    protected PVector sliderXY(String name, float defaultX, float defaultY, float precision) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(name, currentGroup.name)) {
            SliderXY newElement = new SliderXY(currentGroup, name, defaultX, defaultY, precision);
            currentGroup.elements.add(newElement);
        }
        SliderXY slider = (SliderXY) findElement(name, currentGroup.name);
        assert slider != null;
        return slider.value;
    }

    protected PVector sliderXYZ(String name, float value, float precision) {
        return sliderXYZ(name, value, value, value, precision);
    }

    protected PVector sliderXYZ() {
        return sliderXYZ("xyz", 0, 0, 0, 100);
    }

    protected PVector sliderXYZ(String name) {
        return sliderXYZ(name, 100);
    }

    protected PVector sliderXYZ(String name, float precision) {
        return sliderXYZ(name, 0, 0, 0, precision);
    }

    protected PVector sliderXYZ(String name, float x, float y, float z, float precision) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(name, currentGroup.name)) {
            SliderXYZ newElement = new SliderXYZ(currentGroup, name, x, y, z, precision);
            currentGroup.elements.add(newElement);
        }
        SliderXYZ slider = (SliderXYZ) findElement(name, currentGroup.name);
        assert slider != null;
        return slider.value;
    }

    protected int picker() {
        return picker("color");
    }

    protected int picker(String name) {
        return picker(name, 0, 0, 0.1f, 1);
    }

    protected int picker(String name, int grayscale) {
        return picker(name, 0, 0, grayscale);
    }

    protected int picker(String name, float grayscale, float alpha) {
        return picker(name, 0, 0, grayscale, alpha);
    }

    protected int picker(String name, float hue, float sat, float br) {
        return picker(name, hue, sat, br, 1);
    }

    protected int picker(String name, float hue, float sat, float br, float alpha) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(name, currentGroup.name)) {
            ColorPicker newElement = new ColorPicker(currentGroup, name, hue, sat, br, alpha);
            currentGroup.elements.add(newElement);
        }
        ColorPicker slider = (ColorPicker) findElement(name, currentGroup.name);
        assert slider != null;
        return slider.value();
    }

    protected String optionsABC() {
        return options("A", "B", "C");
    }

    protected String options(String defaultValue, String... otherValues) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(defaultValue, currentGroup.name)) {
            Element newElement = new Radio(currentGroup, defaultValue, otherValues);
            currentGroup.elements.add(newElement);
        }
        Radio radio = (Radio) findElement(defaultValue, currentGroup.name);
        assert radio != null;
        return radio.options.get(radio.valueIndex);
    }

    protected boolean button() {
        return button("button");
    }

    protected boolean button(String name) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(name, currentGroup.name)) {
            Button newElement = new Button(currentGroup, name);
            currentGroup.elements.add(newElement);
        }
        Button button = (Button) findElement(name, currentGroup.name);
        return button.value;
    }

    protected boolean toggle() {
        return toggle("toggle");
    }

    protected boolean toggle(String name) {
        return toggle(name, false);
    }

    protected boolean toggle(String name, boolean defaultState) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(name, currentGroup.name)) {
            Toggle newElement = new Toggle(currentGroup, name, defaultState);
            currentGroup.elements.add(newElement);
        }
        Toggle toggle = (Toggle) findElement(name, currentGroup.name);
        return toggle.checked;
    }

    protected void gui() {
        gui(true);
    }

    protected void gui(boolean defaultVisibility) {
        t += radians(1);
        if (frameCount < 3) {
            guiSetup(defaultVisibility);
            return;
        }
        updateKeyboardInput();
        updateMouseState();
        pushStyle();
        pushMatrix();
        strokeCap(SQUARE);
        colorMode(HSB, 1, 1, 1, 1);
        resetMatrixInAnyRenderer();
        updateTrayBackground();
        updateMenuButtons();
        updateGroupsAndTheirElements();
        updateFps();
        if (overlayVisible) {
            overlayOwner.updateOverlay();
        }
        popStyle();
        popMatrix();
        pMousePressed = mousePressed;
        currentGroup = groups.get(0);
    }

    private void handleEscape() {
        if (isKeyPressed(ESC, false)) {

            exit();
        }
    }

    private void updateMouseState() {
        mousePressedOutsideGui = mousePressed && isMouseOutsideGui() && !overlayVisible;
    }

    private void guiSetup(boolean defaultVisibility) {
        if (frameCount == 1) {
            trayVisible = defaultVisibility;
            textSize(textSize * 2);
            registerExitHandler();
        } else if (frameCount == 2) {
            loadLastStateFromFile(true);
        }
    }

    private void registerExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                saveStateToFile();
            }
        }));
    }

    // UTILS

    protected void resetGui() {
        for (Group group : groups) {
            for (Element el : group.elements) {
                el.reset();
            }
        }
    }

    public void rec() {
        rec(g);
    }

    public void rec(PGraphics pg) {
        if (frameCount < frameRecordingEnds) {
            println(frameCount - frameRecordingEnds + recordingFrames + " / " + (recordingFrames - 1));
            pg.save(captureDir + frameCount + ".jpg");
        }
    }

    int numberOfDigitsInFlooredNumber(float inputNumber) {
        return String.valueOf(floor(inputNumber)).length();
    }

    ArrayList<PImage> loadImages(String folderPath) {
        //TODO list all files without Paths.walk
        // https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
        return new ArrayList<PImage>();
    }

    public String regenId() {
        String newId = year() + nf(month(), 2) + nf(day(), 2) + "-" + nf(hour(), 2) + nf(minute(), 2) + nf(second(),
                2) + "_" + this.getClass().getSimpleName();
        captureDir = "out/capture/" + id + "/";
        captureFilename = captureDir + "####.jpg";
        return newId;
    }

    public String randomImageUrl(float width, float height) {
        return "https://picsum.photos/" + floor(width) + "/" + floor(height) + ".jpg";
    }

    public String randomImageUrl(float size) {
        return "https://picsum.photos/" + floor(size) + ".jpg";
    }

    protected boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }


    private float easedAnimation(float startFrame, float duration, float easingFactor) {
        float animationNormalized = constrain(norm(frameCount, startFrame,
                startFrame + duration), 0, 1);
        return ease(animationNormalized, easingFactor);
    }

    protected float ease(float p, float g) {
        if (p < 0.5)
            return 0.5f * pow(2 * p, g);
        else
            return 1 - 0.5f * pow(2 * (1 - p), g);
    }

    // TRAY

    private void updateFps() {
        if (!trayVisible) {
            return;
        }
        int nonFlickeringFrameRate = floor(frameRate > 55 ? 60 : frameRate);
        String fps = nonFlickeringFrameRate + " fps";
        surface.setTitle(this.getClass().getSimpleName() + " " + fps);
    }

    private void updateMenuButtons() {
        float x = 0;
        float y = 0;
        updateMenuButtonHide(x, y, cell * 2, cell * 1.5f);
        if (!trayVisible) {
            return;
        }
        x += cell * 1.5f;
        updateMenuButtonUndo(x, y, cell * 1.5f, cell * 1.5f);
        x += cell * 1.5f;
        updateMenuButtonRedo(x, y, cell * 1.5f, cell * 1.5f);
    }

    private void updateMenuButtonHide(float x, float y, float w, float h) {
        if (activated(MENU_BUTTON_HIDE, x, y, w, h)) {
            trayVisible = !trayVisible;
            trayWidth = trayVisible ? cell * 6 : 0;
            keyboardSelectionIndex = 0;
            undoHideRotationStarted = frameCount;
        }
        float grayscale = (keyboardSelected(MENU_BUTTON_HIDE) || isMouseOver(x, y, w, h)) ? GRAYSCALE_TEXT_SELECTED :
                GRAYSCALE_TEXT_DARK;
        fill(grayscale);
        stroke(grayscale);
        float rotation = easedAnimation(undoHideRotationStarted, MENU_ROTATION_DURATION, MENU_ROTATION_EASING);
        if (trayVisible) {
            rotation += 1;
        }
        if (isMouseOver(x, y, w, h) || trayVisible) {
            displayMenuButtonHide(x, y, w, h, rotation * PI);
        }
    }

    private void displayMenuButtonHide(float x, float y, float w, float h, float rotation) {
        pushMatrix();
        translate(x + w * .5f, y + h * .5f);
        rotate(rotation);
        float arrowWidth = w * .22f;
        line(-arrowWidth, 0, w * .2f, 0);
        strokeWeight(2);
        beginShape();
        vertex(-arrowWidth * .5f, h * .05f);
        vertex(-arrowWidth, 0);
        vertex(-arrowWidth * .5f, -h * .05f);
        endShape(CLOSE);
        popMatrix();
    }

    private void updateMenuButtonUndo(float x, float y, float w, float h) {
        boolean canUndo = undoStack.size() > 0;
        if (canUndo && (activated(MENU_BUTTON_UNDO, x, y, w, h) || actions.contains(ACTION_UNDO))) {
            pushCurrentStateToRedo();
            popUndoToCurrentState();
            undoRotationStarted = frameCount;
        }
        float rotation = easedAnimation(undoRotationStarted, MENU_ROTATION_DURATION, MENU_ROTATION_EASING);
        displayMenuButton(x, y, w, h, rotation * TWO_PI, false, MENU_BUTTON_UNDO, undoStack.size());
    }

    private void updateMenuButtonRedo(float x, float y, float w, float h) {
        boolean canRedo = redoStack.size() > 0;
        if (canRedo && (activated(MENU_BUTTON_REDO, x, y, w, h) || actions.contains(ACTION_REDO))) {
            pushCurrentStateToUndoManually();
            popRedoToCurrentState();
            redoRotationStarted = frameCount;
        }
        float rotation = easedAnimation(redoRotationStarted, MENU_ROTATION_DURATION, MENU_ROTATION_EASING);
        displayMenuButton(x, y, w, h, rotation * TWO_PI, true, MENU_BUTTON_REDO, redoStack.size());
    }

    private void displayMenuButton(float x, float y, float w, float h, float rotation,
                                   boolean direction, String menuButtonType, int stackSize) {
        if (stackSize == 0) {
            return;
        }
        textSize(textSize);
        textAlign(CENTER, CENTER);
        float grayscale = (keyboardSelected(menuButtonType) || isMouseOver(x, y, w, h)) ?
                GRAYSCALE_TEXT_SELECTED : GRAYSCALE_TEXT_DARK;
        fill(grayscale);
        pushMatrix();
        translate(x + w * .5f, y + h * .5f);
        rotate(PI + (direction ? rotation : -rotation));
        float margin = 0;
        noFill();
        stroke(grayscale);
        strokeWeight(2);
        float radiusMultiplier = .5f;
        arc(0, 0, w * radiusMultiplier, h * radiusMultiplier, margin, PI - margin);
        fill(grayscale);
        stroke(grayscale);
        beginShape();
        vertex((direction ? -1 : 1) * w * radiusMultiplier * .4f, h * .1f);
        vertex((direction ? -1 : 1) * w * radiusMultiplier * .5f, 0);
        vertex((direction ? -1 : 1) * w * radiusMultiplier * .55f, h * .1f);
        endShape();

        popMatrix();
    }

    protected void group(String name) {
        Group group = findGroup(name);
        if (!groupExists(name)) {
            group = new Group(name);
            groups.add(group);
        }
        setCurrentGroup(group);
    }

    private void updateGroupsAndTheirElements() {
        float x = cell * .5f;
        float y = cell * 2.5f;
        boolean wasAnyElementActivated = false;
        for (Group group : groups) {
            if (group.elements.isEmpty()) {
                continue;
            }
            group.update(y);
            if (trayVisible) {
                group.displayInTray(x, y);
            }
            if (group.expanded) {
                x += cell * .5f;
                for (Element el : group.elements) {
                    y += cell;
                    if (el.equals(overlayOwner)) {
                        el.handleKeyboardInput();
                    }
                    if (updateElement(group, el, y)) {
                        wasAnyElementActivated = true;
                    }
                    if (trayVisible) {
                        displayElement(group, el, x, y);
                    }
                }
                x -= cell * .5f;
            }
            y += cell;
        }
        if (!wasAnyElementActivated && actions.contains(ACTION_ACTIVATE)) {
            if (overlayVisible) {
                overlayVisible = false;
            }
        }
    }

    private boolean updateElement(Group group, Element el, float y) {
        el.update();
        if (activated(group.name + el.name, 0, y - cell, trayWidth, cell)) {
            if (!el.canHaveOverlay()) {
                el.onActivationWithoutOverlay(0, y - cell, trayWidth, cell);
                return true;
            }
            if (!overlayVisible) {
                setOverlayOwner(el);
                return true;
            } else if (overlayVisible && !el.equals(overlayOwner)) {
                setOverlayOwner(el);
                return true;
            } else if (overlayVisible && el.equals(overlayOwner)) {
                overlayVisible = false;
            }
        }
        return false;
    }

    private void displayElement(Group group, Element el, float x, float y) {
        boolean isSelected = keyboardSelected(group.name + el.name) ||
                isMouseOver(0, y - cell, trayWidth, cell);
        float grayScale;
        if (isSelected) {
            el.lastSelected = frameCount;
            grayScale = GRAYSCALE_TEXT_SELECTED;
        } else {
            float deselectionFadeout = easedAnimation(el.lastSelected, DESELECTION_FADEOUT_DURATION,
                    DESELECTION_FADEOUT_EASING);
            grayScale = lerp(GRAYSCALE_TEXT_DARK, GRAYSCALE_TEXT_SELECTED, 1 - deselectionFadeout);
        }
        fill(grayScale);
        stroke(grayScale);
        el.displayOnTray(x, y);
    }

    private void updateTrayBackground() {
        if (!trayVisible) {
            return;
        }
        textSize(textSize);
        trayWidth = max(minimumTrayWidth, findLongestNameWidth() + cell * 2);
        noStroke();
        fill(0, BACKGROUND_ALPHA);
        rectMode(CORNER);
        rect(0, 0, trayWidth, height);
    }

    private void displayTrayGrid() {
        stroke(GRAYSCALE_GRID);
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

    private void setOverlayOwner(Element overlayOwnerToSet) {
        this.overlayOwner = overlayOwnerToSet;
        this.overlayOwner.onOverlayShown();
        overlayVisible = true;
        underlineTrayAnimationStarted = frameCount;
    }

    // INPUT

    private boolean activated(String query, float x, float y, float w, float h) {
        return ((trayVisible || query.equals(MENU_BUTTON_HIDE)) && mouseJustReleasedHere(x, y, w, h)) || keyboardActivated(query);
    }

    private boolean keyboardActivated(String query) {
        return (overlayOwner != null && (overlayOwner.parent + overlayOwner.name).equals(query) || keyboardSelected(query))
                && actions.contains(ACTION_ACTIVATE);
    }

    private boolean mouseJustReleasedHere(float x, float y, float w, float h) {
        return mouseJustReleased() && isPointInRect(mouseX, mouseY, x, y, w, h);
    }

    private boolean mouseJustReleased() {
        return pMousePressed && !mousePressed;
    }

    private boolean mouseJustPressed() {
        return !pMousePressed && mousePressed;
    }

    private boolean mouseJustPressedOutsideGui() {
        return !pMousePressed && mousePressed && isMouseOutsideGui();
    }

    private boolean isMouseOutsideGui() {
        return !trayVisible || !isPointInRect(mouseX, mouseY, 0, 0, trayWidth, height);
    }

    private boolean isMouseOver(float x, float y, float w, float h) {
        return frameCount > 1 && isPointInRect(mouseX, mouseY, x, y, w, h);
    }

    public void mouseWheel(MouseEvent event) {
        float direction = event.getCount();
        if (direction < 0) {
            actions.add(ACTION_PRECISION_ZOOM_IN);
        } else if (direction > 0) {
            actions.add(ACTION_PRECISION_ZOOM_OUT);
        }
    }

    private boolean isAnyGroupKeyboardSelected() {
        return findKeyboardSelectedGroup() != null;
    }

    private boolean isAnyElementKeyboardSelected() {
        return findKeyboardSelectedElement() != null;
    }

    private boolean keyboardSelected(String query) {
        if (!keyboardActive) {
            return false;
        }
        if ((query.equals(MENU_BUTTON_HIDE) && keyboardSelectionIndex == 0)
                || (query.equals(MENU_BUTTON_UNDO) && keyboardSelectionIndex == 1)
                || (query.equals(MENU_BUTTON_REDO) && keyboardSelectionIndex == 2)) {
            return true;
        }
        int i = MENU_BUTTON_COUNT;
        for (Group group : groups) {
            if (group.name.equals(query) && keyboardSelectionIndex == i) {
                return true;
            }
            i++;
            for (Element el : group.elements) {
                if ((group.name + el.name).equals(query) && keyboardSelectionIndex == i) {
                    if (upAndDownArrowsControlOverlay() && !el.equals(overlayOwner)) {
                        return false;
                    }
                    if (el.equals(overlayOwner)) {
                        return true;
                    }
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    private int keyboardSelectionLength() {
        int elementCount = 0;
        for (Group group : groups) {
            elementCount += group.elements.size();
        }
        return MENU_BUTTON_COUNT + groups.size() + elementCount;
    }

    public void mousePressed() {
        if (!upAndDownArrowsControlOverlay()) {
            keyboardActive = false;
        }
    }

    public void keyPressed() {
        println(String.valueOf(key), keyCode, key == CODED);
        keyboardActive = true;
        if (key == CODED) {
            if (!isKeyPressed(keyCode, true)) {
                keyboardKeys.add(new Key(keyCode, true));
            }
        } else {
            if (!isKeyPressed(key, false)) {
                keyboardKeys.add(new Key((int) key, false));
            }
        }
        if (key == 'k') {
            frameRecordingEnds = frameCount + recordingFrames + 1;
            id = regenId();
        }
        if (key == 'i') {
            frameRecordingEnds = frameCount + 2;
            id = regenId();
        }
        if (key == 'c') {
            frameRecordingEnds = frameCount - 1;
        }
    }

    private boolean isKeyPressed(int keyCode, boolean coded) {
        for (Key kk : keyboardKeys) {
            if (kk.character == keyCode && kk.coded == coded) {
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

    private void updateKeyboardInput() {
        previousActions.clear();
        previousActions.addAll(actions);
        actions.clear();
        int lastIndex = keyboardSelectionIndex;
        for (Key kk : keyboardKeys) {
            if (kk.coded) {
                if (kk.character == UP) {
                    actions.add(ACTION_UP);
                    if (!upAndDownArrowsControlOverlay() && kk.repeatCheck()) {
                        if (keyboardSelectionIndex == MENU_BUTTON_COUNT) {
                            keyboardSelectionIndex = 0;
                        } else {
                            int skipped = hiddenElementCount(false);
                            keyboardSelectionIndex -= skipped;
                            keyboardSelectionIndex--;
                        }
                    }
                }
                if (kk.character == DOWN) {
                    actions.add(ACTION_DOWN);
                    if (!upAndDownArrowsControlOverlay() && kk.repeatCheck()) {
                        if (keyboardSelectionIndex < MENU_BUTTON_COUNT) {
                            keyboardSelectionIndex = MENU_BUTTON_COUNT;
                        } else {
                            int skipped = hiddenElementCount(true);
                            keyboardSelectionIndex += skipped;
                            keyboardSelectionIndex++;
                        }
                    }
                }
                if (kk.character == LEFT) {
                    if (isAnyGroupKeyboardSelected() && findKeyboardSelectedGroup().expanded) {
                        Group keyboardSelected = findKeyboardSelectedGroup();
                        keyboardSelected.expanded = !keyboardSelected.expanded;
                    } else {
                        actions.add(ACTION_LEFT);
                    }
                }
                if (kk.character == RIGHT) {
                    if (isAnyGroupKeyboardSelected() && !findKeyboardSelectedGroup().expanded) {
                        Group keyboardSelected = findKeyboardSelectedGroup();
                        keyboardSelected.expanded = !keyboardSelected.expanded;
                    } else {
                        actions.add(ACTION_RIGHT);
                    }
                }
                if (kk.character == CONTROL) {
                    actions.add(ACTION_CONTROL);
                }
                if (kk.character == ALT) {
                    actions.add(ACTION_ALT);
                }
            } else if (!kk.coded) {
                if (!kk.justPressed) {
                    continue;
                }
                if (kk.character == '*' || kk.character == '+') {
                    actions.add(ACTION_PRECISION_ZOOM_IN);
                }
                if (kk.character == '/' || kk.character == '-') {
                    actions.add(ACTION_PRECISION_ZOOM_OUT);
                }
                if (kk.character == ' ' || kk.character == ENTER) {
                    actions.add(ACTION_ACTIVATE);
                }
                if (kk.character == 'r') {
                    actions.add(ACTION_RESET);
                }
                if (kk.character == 'h') {
                    trayVisible = !trayVisible;
                }
                if (kk.character == 'z') {
                    actions.add(ACTION_UNDO);
                }
                if (kk.character == 'y') {
                    actions.add(ACTION_REDO);
                }
                if (kk.character == 19) {
                    // CTRL + S
                    saveStateToFile();
                }
                if (kk.character == 12) {
                    // CTRL + L
                    loadLastStateFromFile(true);
                }
                if (kk.character == 26) {
                    // CTRL + Z
                    actions.add(ACTION_UNDO);
                }
                if (kk.character == 25) {
                    // CTRL + Y
                    actions.add(ACTION_REDO);
                }
            }
            kk.justPressed = false;
        }
        if (keyboardSelectionIndex >= keyboardSelectionLength()) {
            keyboardSelectionIndex = MENU_BUTTON_COUNT;
        }
        if (keyboardSelectionIndex < MENU_BUTTON_COUNT) {
            Group lastGroup = getLastGroup();
            if (!lastGroup.expanded) {
                keyboardSelectionIndex = keyboardSelectionLength() - lastGroup.elements.size() - 1;
            } else {
                keyboardSelectionIndex = keyboardSelectionLength() - 1;
            }
        }
/* //debug

        if (lastIndex != keyboardSelectionIndex) {
            Element el = findKeyboardSelectedElement();
            Group group = findKeyboardSelectedGroup();
            String name = "-";
            if(el != null){
                name = el.name;
            }
            if(group != null){
                name = group.name;
            }

            println("before", lastIndex,
                    "after", keyboardSelectionIndex,
                    "length", keyboardSelectionLength(),
                    "current ", name);

        }
*/
    }

    private boolean upAndDownArrowsControlOverlay() {
        return overlayVisible && (verticalOverlayVisible || pickerOverlayVisible);
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

    // GROUP AND ELEMENT HANDLING
    private int hiddenElementCount(boolean forwardFacing) {
        Group group = findKeyboardSelectedGroup();
        Element element = findKeyboardSelectedElement();
        if (previousActions.contains(ACTION_ALT)) {
            if (element != null) {
                group = findKeyboardSelectedElement().parent;
                if (forwardFacing) {
                    return group.elements.size() - group.elements.indexOf(element) - 1;
                } else {
                    return group.elements.indexOf(element);
                }
            } else {
                if (forwardFacing) {
                    return group.elements.size();
                } else {
                    if (group != null) {
                        Group previous = findPreviousGroup(group.name);
                        if (previous != null) {
                            return previous.elements.size();
                        }
                    }
                    return 0;
                }
            }
        } else if (group != null) {
            if (forwardFacing && !group.expanded) {
                return group.elements.size();
            }
            if (!forwardFacing) {
                Group previous = findPreviousGroup(group.name);
                if (previous.expanded) {
                    return 0;
                }
                return previous.elements.size();
            }
        }

        return 0;
    }

    private Group getCurrentGroup() {
        if (currentGroup == null) {
            if (groups.isEmpty()) {
                Group anonymous = new Group(this.getClass().getSimpleName());
                groups.add(anonymous);
                currentGroup = anonymous;
            } else {
                return groups.get(0);
            }
        }
        return currentGroup;
    }

    private void setCurrentGroup(Group currentGroup) {
        this.currentGroup = currentGroup;
    }

    private Group getLastGroup() {
        return groups.get(groups.size() - 1);
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

    private Group findPreviousGroup(String query) {
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

    private Group findNextGroup(String query) {
        if (groups.isEmpty()) {
            return null;
        }
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
        return groups.get(0);
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

    // STATE

    private void popUndoAndForget() {
        if (undoStack.size() == 0) {
            return;
        }
        undoStack.remove(undoStack.size() - 1);
    }

    private void pushCurrentStateToRedo() {
        redoStack.add(getGuiState());
    }

    private void pushCurrentStateToUndoNaturally() {
        redoStack.clear();
        undoStack.add(getGuiState());
    }

    private void pushStateToUndoManually(ArrayList<String> state) {
        setGuiState(state);
        pushCurrentStateToUndoNaturally();
    }

    private void pushStateToRedoManually(ArrayList<String> state) {
        redoStack.add(state);
    }

    private void pushCurrentStateToUndoManually() {
        undoStack.add(getGuiState());
    }

    private void popAllUndo() {
        int i = undoStack.size() - 1;
        while (undoStack.size() > 1) {
            undoStack.remove(i--);
        }
    }

    private void popAllRedo() {
        int i = redoStack.size() - 1;
        while (redoStack.size() > 1) {
            redoStack.remove(i--);
        }
    }

    private void popUndoToCurrentState() {
        if (undoStack.size() == 0) {
            throw new IllegalStateException("nothing to pop in undo stack!");
        }
        setGuiState(undoStack.remove(undoStack.size() - 1));
    }

    private void popRedoToCurrentState() {
        if (redoStack.size() == 0) {
            throw new IllegalStateException("nothing to pop in redo stack!");
        }
        setGuiState(redoStack.remove(redoStack.size() - 1));
    }

    private ArrayList<String> getGuiState() {
        ArrayList<String> states = new ArrayList<String>();
        for (Group group : groups) {
            states.add(group.getState());
            for (Element el : group.elements) {
                states.add(el.getState());
            }
        }
        return states;
    }

    private void setGuiState(ArrayList<String> statesToSet) {
        for (String state : statesToSet) {
            String[] splitState = state.split(INNER_SEPARATOR);
            if (state.startsWith(GROUP_PREFIX)) {
                Group group = findGroup(splitState[1]);
                if (group == null) {
                    //TODO lazy init here using the state data
                    continue;
                }
//                println("setting " + group.name + " to " + state);
                group.setState(state);
            } else {
                Element el = findElement(splitState[1], splitState[0]);
                if (el == null) {
                    continue;
                }
//                println("setting " + el.name + " to " + state);
                el.setState(state);
            }
        }
    }

    void saveStateToFile() {
        if (undoStack.size() == 0 && redoStack.size() == 0) {
            return;
        }
        String[] loadedLines = loadLastStateFromFile(false);
        File file = dataFile(settingsPath());
        pushCurrentStateToUndoNaturally();
        println("settings saved to " + file.getAbsolutePath());
        ArrayList<String> save = new ArrayList<String>(Arrays.asList(loadedLines));
        save.add(SAVE_PREFIX);
        save.add(UNDO_PREFIX);
        for (ArrayList<String> guiStates : undoStack) {
            save.add(STATE_BEGIN);
            save.addAll(guiStates);
            save.add(STATE_END);
        }
        save.add(REDO_PREFIX);
        for (ArrayList<String> guiState : redoStack) {
            save.add(STATE_BEGIN);
            save.addAll(guiState);
            save.add(STATE_END);
        }
        String[] saveArray = new String[save.size()];
        for (int i = 0; i < save.size(); i++) {
            saveArray[i] = save.get(i);
        }
        saveStrings(file, saveArray);
    }

    String[] loadLastStateFromFile(boolean alsoPush) {
        File file = dataFile(settingsPath());
        if (!file.exists()) {
            return new String[0];
        }
        String[] lines = loadStrings(file);
        if (alsoPush) {
            popAllRedo();
            popAllUndo();
            int lastSaveStartIndex = findLastSaveStartIndex(lines);
            boolean pushingUndo = false;
            ArrayList<String> runningState = new ArrayList<String>();
            for (int i = lastSaveStartIndex + 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.startsWith(UNDO_PREFIX)) {
                    pushingUndo = true;
                } else if (line.startsWith(REDO_PREFIX)) {
                    pushingUndo = false;
                } else if (line.startsWith(STATE_BEGIN)) {
                    runningState.clear();
                } else if (line.startsWith(STATE_END)) {
                    if (pushingUndo) {
//                        println("pushing ", concat(runningState));
                        pushStateToUndoManually(runningState);
                        runningState.clear();
                    } else {
//                        println("pushing ", concat(runningState));
                        pushStateToRedoManually(runningState);
                        runningState.clear();
                    }
                } else {
                    runningState.add(line);
                }
            }
            popUndoToCurrentState();
        }
        return lines;
    }

    private String concat(ArrayList<String> guiState) {
        StringBuilder sb = new StringBuilder();
        for (String s : guiState) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }

    private String concat(String[] guiState) {
        StringBuilder sb = new StringBuilder();
        for (String s : guiState) {
            sb.append(s);
        }
        return sb.toString();
    }

    private int findLastSaveStartIndex(String[] lines) {
        int result = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith(SAVE_PREFIX)) {
                result = i;
            }
        }
//        println("last line with " + SAVE_PREFIX + " was " + result);
        return result;
    }

    private String settingsPath() {
        return "gui_settings\\" + year() + nf(month(), 2, 0) + nf(day(), 2, 0)
                + "_" + this.getClass().getSimpleName() + ".txt";
    }

// CLASSES

    class Key {
        boolean justPressed;
        boolean repeatedAlready = false;
        boolean coded;
        int character;
        int lastRegistered = -1;

        Key(Integer character, boolean coded) {
            this.character = character;
            this.coded = coded;
            this.justPressed = true;
        }

        boolean repeatCheck() {
            boolean shouldApply = justPressed ||
                    (!repeatedAlready && millis() > lastRegistered + KEY_REPEAT_DELAY_FIRST) ||
                    (repeatedAlready && millis() > lastRegistered + KEY_REPEAT_DELAY);
            if (shouldApply) {
                lastRegistered = millis();
                if (!justPressed) {
                    repeatedAlready = true;
                }
            }
            justPressed = false;
            return shouldApply;
        }
    }

    class Group {
        String name;
        boolean expanded = true;
        ArrayList<Element> elements = new ArrayList<Element>();

        Group(String name) {
            this.name = name;
        }

        public boolean update(float y) {
            if (activated(name, 0, y - cell, trayWidth, cell)) {
                expanded = !expanded;
                return true;
            }
            return false;
        }

        public void displayInTray(float x, float y) {
            fill((keyboardSelected(name) || isMouseOver(0, y - cell, trayWidth, cell)) ?
                    GRAYSCALE_TEXT_SELECTED : GRAYSCALE_TEXT_DARK);
            textAlign(LEFT, BOTTOM);
            textSize(textSize);
            text(name, x, y);
        }

        public String getState() {
            return GROUP_PREFIX + INNER_SEPARATOR + name + INNER_SEPARATOR + expanded;
        }

        public void setState(String state) {
            String[] split = state.split(INNER_SEPARATOR);
            expanded = Boolean.parseBoolean(split[2]);
        }
    }

    abstract class Element {
        public float lastSelected = -DESELECTION_FADEOUT_DURATION;
        Group parent;
        String name;

        Element(Group parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        void reset() {

        }

        abstract boolean canHaveOverlay();

        String getState() {
            return parent.name + INNER_SEPARATOR + name + INNER_SEPARATOR;
        }

        void setState(String newState) {

        }

        void update() {

        }

        void updateOverlay() {

        }

        void onOverlayShown() {

        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {

        }

        void displayOnTray(float x, float y) {
            displayOnTray(x, y, name);
        }

        void displayOnTray(float x, float y, String text) {
            textAlign(LEFT, BOTTOM);
            textSize(textSize);
            if (overlayVisible && this.equals(overlayOwner)) {
                underlineAnimation(underlineTrayAnimationStarted, UNDERLINE_TRAY_ANIMATION_DURATION, x, y, true);
            }
            text(text, x, y);
        }

        float trayTextWidth() {
            return textWidth(name);
        }

        void underlineAnimation(float startFrame, float duration, float x, float y, boolean stayExtended) {
            float fullWidth = textWidth(name);
            float animation = easedAnimation(startFrame, duration, UNDERLINE_TRAY_ANIMATION_EASING);
            if (!stayExtended && animation == 1) {
                animation = 0;
            }
            float w = fullWidth * animation;
            float centerX = x + fullWidth * .5f;
            strokeWeight(2);
            line(centerX - w * .5f, y, centerX + w * .5f, y);
        }

        void displayCheckMarkOnTray(float x, float y, float animation, boolean fadeIn, boolean displayBox) {
            float w = previewTrayBoxWidth;
            pushMatrix();
            translate(x - previewTrayBoxMargin, previewTrayBoxOffsetY);
            noFill();
            if (displayBox) {
                rectMode(CENTER);
                pushStyle();
                strokeWeight(1);
                stroke(GRAYSCALE_TEXT_DARK);
                rect(-w, y - textSize * .5f, w, w);
                popStyle();
            }
            strokeWeight(2);
            beginShape();
            int detail = 30;
            float checkMarkTopLeftX = -w * 1.25f;
            float checkMarkTopLeftY = y - textSize * .6f;
            float lowestCheckMarkPointX = -w;
            float lowestCheckMarkPointY = y - textSize * .4f;
            float checkMarkTopRightX = 0;
            float checkMarkTopRightY = y - textSize * .9f;
            for (int i = 0; i < detail; i++) {
                float inorm = norm(i, 0, detail - 1);
                if ((fadeIn && inorm > animation) || (!fadeIn && inorm < animation)) {
                    continue;
                }
                if (inorm < .333f) {
                    float downwardStroke = norm(inorm, 0, .333f);
                    float downwardX = lerp(checkMarkTopLeftX, lowestCheckMarkPointX, downwardStroke);
                    float downwardY = lerp(checkMarkTopLeftY, lowestCheckMarkPointY, downwardStroke);
                    vertex(downwardX, downwardY);
                    continue;
                }
                float upwardStroke = norm(inorm, .333f, 1);
                float upwardX = lerp(lowestCheckMarkPointX, checkMarkTopRightX, upwardStroke);
                float upwardY = lerp(lowestCheckMarkPointY, checkMarkTopRightY, upwardStroke);
                vertex(upwardX, upwardY);
            }
            endShape();
            popMatrix();
        }

        void handleKeyboardInput() {
        }
    }

    class Radio extends Element {
        ArrayList<String> options = new ArrayList<String>();
        int valueIndex = 0;
        float optionWidth = 0;

        Radio(Group parent, String name, String[] options) {
            super(parent, name);
            this.options.add(name);
            this.options.addAll(Arrays.asList(options));
        }

        String getState() {
            StringBuilder state = new StringBuilder(super.getState() + valueIndex);
            for (String option : options) {
                state.append(INNER_SEPARATOR);
                state.append(option);
            }
            return state.toString();
        }

        void setState(String newState) {
            valueIndex = Integer.parseInt(newState.split(INNER_SEPARATOR)[2]);
        }

        protected void reset() {
            valueIndex = 0;
        }

        boolean canHaveOverlay() {
            return false;
        }

        String value() {
            return options.get(valueIndex);
        }

        void displayOnTray(float x, float y) {
            super.displayOnTray(x, y, value());
            displayDotsOnTray(x, y);
        }

        private void displayDotsOnTray(float x, float y) {
            for (int i = 0; i < options.size(); i++) {
                float iNorm = norm(i, 0, options.size() - 1);
                float dotX = x + i*10;
                pushStyle();
                fill(i == valueIndex ? GRAYSCALE_TEXT_SELECTED : GRAYSCALE_TEXT_DARK);
                noStroke();
                rectMode(CORNER);
                rect(dotX + 2.5f, y, 5, 5);
                pushStyle();
            }
        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {
            pushCurrentStateToUndoNaturally();
            valueIndex++;
            if (valueIndex >= options.size()) {
                valueIndex = 0;
            }
        }

        float trayTextWidth() {
            return textWidth(value());
        }
    }

    class Button extends Element {
        boolean value;
        private float activationStarted = -CHECK_ANIMATION_DURATION * 2;

        Button(Group parent, String name) {
            super(parent, name);
        }

        boolean canHaveOverlay() {
            return false;
        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {
            value = true;
            activationStarted = frameCount;
        }

        void displayOnTray(float x, float y) {
            float checkMarkAnimation = easedAnimation(activationStarted, CHECK_ANIMATION_DURATION * 2,
                    CHECK_ANIMATION_EASING);
            if (checkMarkAnimation > 0 && checkMarkAnimation < 1) {
                if (checkMarkAnimation < .5) {
                    displayCheckMarkOnTray(x, y, checkMarkAnimation * 2, true, false);
                } else {
                    displayCheckMarkOnTray(x, y, (checkMarkAnimation - .5f) * 2, false, false);
                }
            }
            super.displayOnTray(x, y);
        }

        void update() {
            value = false;
        }
    }

    class Toggle extends Element {
        boolean checked, checkByDefault;
        private float activationStarted = -UNDERLINE_TRAY_ANIMATION_DURATION;

        Toggle(Group parent, String name, boolean initialState) {
            super(parent, name);
            this.checkByDefault = initialState;
            this.checked = initialState;
        }

        String getState() {
            return super.getState() + checked;
        }

        void setState(String newState) {
            this.checked = Boolean.parseBoolean(newState.split(INNER_SEPARATOR)[2]);
        }

        boolean canHaveOverlay() {
            return false;
        }

        void displayOnTray(float x, float y) {
            float checkMark = easedAnimation(activationStarted, CHECK_ANIMATION_DURATION, CHECK_ANIMATION_EASING);
            displayCheckMarkOnTray(x, y, checkMark, checked, true);
            super.displayOnTray(x, y);
        }

        void reset() {
            checked = checkByDefault;
        }

        void update() {
            if (overlayOwner != null && overlayOwner.equals(this) && actions.contains(ACTION_RESET)) {
                reset();
            }
        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {
            pushCurrentStateToUndoNaturally();
            activationStarted = frameCount;
            checked = !checked;
        }
    }

    abstract class Slider extends Element {
        Slider(Group parent, String name) {
            super(parent, name);
        }

        protected float updateFullHorizontalSlider(float x, float y, float w, float h, float value, float precision,
                                                   float horizontalRevealAnimationStarted, boolean alternative) {
            float deltaX = updateInfiniteSlider(precision, width, true, true, alternative);
            float horizontalAnimation = easedAnimation(horizontalRevealAnimationStarted - SLIDER_REVEAL_START_SKIP,
                    SLIDER_REVEAL_DURATION, SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(x + width * .5f, y, w, h,
                    precision, value, horizontalAnimation, true, true);
            return deltaX;
        }

        @SuppressWarnings("SuspiciousNameCombination")
        protected float updateFullHeightVerticalSlider(float x, float y, float w, float h, float value, float precision,
                                                       float verticalRevealAnimationStarted, boolean alternative) {
            float deltaY = updateInfiniteSlider(precision, height, false, true, alternative);
            float verticalAnimation = easedAnimation(verticalRevealAnimationStarted - SLIDER_REVEAL_START_SKIP,
                    SLIDER_REVEAL_DURATION, SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(x + height * .5f, y, w, h,
                    precision, value, verticalAnimation, false, true);
            return deltaY;
        }

        protected float updateInfiniteSlider(float precision, float sliderWidth, boolean horizontal, boolean reversed,
                                             boolean alternative) {
            if (mousePressed && isMouseOutsideGui()) {
                float screenSpaceDelta = horizontal ? (pmouseX - mouseX) : (pmouseY - mouseY);
                if (reversed) {
                    screenSpaceDelta *= -1;
                }
                float valueSpaceDelta = screenDistanceToValueDistance(screenSpaceDelta, precision);
                if (valueSpaceDelta != 0) {
                    return valueSpaceDelta;
                }
            }
            if (previousActions.contains(ACTION_ALT) && previousActions.contains(ACTION_CONTROL)) {
                return keyboardDelta(true, horizontal, precision, sliderWidth);
            }
            if ((alternative && !previousActions.contains(ACTION_ALT)) ||
                    (!alternative && previousActions.contains(ACTION_ALT))) {
                return 0;
            }
            return keyboardDelta(false, horizontal, precision, sliderWidth);
        }

        private float keyboardDelta(boolean directionless, boolean horizontal, float precision, float sliderWidth) {
            if (actions.contains(ACTION_LEFT) && (horizontal || directionless)) {
                return screenDistanceToValueDistance(-3, precision);
            }
            if (actions.contains(ACTION_RIGHT) && (horizontal || directionless)) {
                return screenDistanceToValueDistance(3, precision);
            }
            if (actions.contains(ACTION_UP) && (!horizontal || directionless)) {
                return screenDistanceToValueDistance(-3, precision);
            }
            if (actions.contains(ACTION_DOWN) && (!horizontal || directionless)) {
                return screenDistanceToValueDistance(3, precision);
            }
            return 0f;
        }

        float screenDistanceToValueDistance(float screenSpaceDelta, float precision) {
            float valueToScreenRatio = precision / width;
            return screenSpaceDelta * valueToScreenRatio;
        }

        void displayInfiniteSliderCenterMode(float x, float y, float w, float h, float precision, float value,
                                             float revealAnimation, boolean horizontal,
                                             boolean cutout) {
            float markerHeight = h * revealAnimation;
            pushMatrix();
            pushStyle();
            if (!horizontal) {
                translate(width * .5f, height * .5f);
                rotate(-HALF_PI);
                translate(-height * .5f, -width * .5f);
            }
            translate(x, y);
            noStroke();
            drawSliderBackground(w, h, cutout, horizontal);
            float weight = 2;
            strokeWeight(weight);
            drawHorizontalLine(w, weight, revealAnimation);
            if (!horizontal) {
                pushMatrix();
                scale(-1, 1);
            }
            drawMarkerLines(precision * 0.5f, 0, markerHeight * .6f, weight * revealAnimation,
                    true, value, precision, w, h, !horizontal, revealAnimation);
            drawMarkerLines(precision * .05f, 10, markerHeight * .3f, weight * revealAnimation,
                    false, value, precision, w, h, !horizontal, revealAnimation);
            if (!horizontal) {
                popMatrix();
            }
            drawValue(h, precision, value, revealAnimation);
            popMatrix();
            popStyle();
        }

        void drawSliderBackground(float w, float h, boolean cutout, boolean horizontal) {
            fill(0, BACKGROUND_ALPHA);
            rectMode(CENTER);
            float xOffset = 0;
            if (cutout) {
                xOffset = horizontal ? trayWidth : h;
            }
            rect(xOffset, 0, w, h);
        }

        void drawHorizontalLine(float w, float weight, float revealAnimation) {
            stroke(GRAYSCALE_TEXT_DARK);
            beginShape();
            w *= revealAnimation;
            for (int i = 0; i < w; i++) {
                float iNorm = norm(i, 0, w);
                float screenX = lerp(-w, w, iNorm);
                stroke(GRAYSCALE_TEXT_SELECTED, darkenEdges(screenX, w));
                vertex(screenX, 0);
            }
            endShape();
        }

        void drawMarkerLines(float frequency, int skipEveryNth, float markerHeight, float horizontalLineHeight,
                             boolean shouldDrawValue, float value, float precision, float w, float h,
                             boolean flipTextHorizontally, float revealAnimationEased) {
            float markerValue = -precision - value - frequency;
            int i = 0;
            while (markerValue < precision - value) {
                markerValue += frequency;
                if (skipEveryNth != 0 && i++ % skipEveryNth == 0) {
                    continue;
                }
                float markerNorm = norm(markerValue, -precision - value, precision - value);
                drawMarkerLine(markerValue, precision, w, h, markerHeight, horizontalLineHeight, value,
                        shouldDrawValue, flipTextHorizontally,
                        revealAnimationEased);
            }
        }

        void drawMarkerLine(float markerValue, float precision, float w, float h, float markerHeight,
                            float horizontalLineHeight,
                            float value, boolean shouldDrawValue, boolean flipTextHorizontally,
                            float revealAnimationEased) {
            float moduloValue = markerValue;
            while (moduloValue > precision) {
                moduloValue -= precision * 2;
            }
            while (moduloValue < -precision) {
                moduloValue += precision * 2;
            }
            float screenX = map(moduloValue, -precision, precision, -w, w);
            float grayscale = darkenEdges(screenX, w);
            fill(GRAYSCALE_TEXT_SELECTED, grayscale * revealAnimationEased);
            stroke(GRAYSCALE_TEXT_SELECTED, grayscale * revealAnimationEased);
            line(screenX, -markerHeight * .5f, screenX, -horizontalLineHeight * .5f);
            if (shouldDrawValue) {
                if (flipTextHorizontally) {
                    pushMatrix();
                    scale(-1, 1);
                }
                float displayValue = moduloValue + value;
                String displayText = nf(displayValue, 0, 0);
                if (displayText.equals("-0")) {
                    displayText = "0";
                }
                pushMatrix();
                textAlign(CENTER, CENTER);
                textSize(textSize);
                float textX = screenX + ((displayText.equals("0") || displayValue > 0) ? 0 : -textWidth("-") * .5f);
                text(displayText, flipTextHorizontally ? -textX : textX, h * .25f);
                if (flipTextHorizontally) {
                    popMatrix();
                }
                popMatrix();
            }
        }

        void drawValue(float sliderHeight, float precision, float value, float animationEased) {
            fill(GRAYSCALE_TEXT_DARK);
            textAlign(CENTER, CENTER);
            textSize(textSize * 1.5f);
            float textY = -cell * 2.5f;
            float textX = 0;
            String text = nf(value, 0, 2);
            if (text.startsWith("-")) {
                textX -= textWidth("-") * .5f;
            }
            noStroke();
            fill(0, BACKGROUND_ALPHA);
            rectMode(CENTER);
            rect(textX, textY + textSize * .33f, textWidth(text) + 20, textSize * 2 + 20);
            fill(GRAYSCALE_TEXT_SELECTED * animationEased);
            text(text, textX, textY);
        }

        float darkenEdges(float screenX, float w) {
            float xNorm = norm(screenX, -w, w);
            float distanceFromCenter = abs(.5f - xNorm) * 4;
            return 1 - ease(distanceFromCenter, SLIDER_EDGE_DARKEN_EASING);
        }

        void recordStateForUndo() {
            if (mouseJustPressedOutsideGui() || keyboardInteractionJustStarted()) {
                pushCurrentStateToUndoNaturally();
            }
        }

        boolean keyboardInteractionJustStarted() {
            boolean wasKeyboardActive = previousActions.contains(ACTION_LEFT) || previousActions.contains(ACTION_RIGHT);
            boolean isKeyboardActive = actions.contains(ACTION_LEFT) || actions.contains(ACTION_RIGHT);
            return !wasKeyboardActive && isKeyboardActive;
        }
    }

    class SliderFloat extends Slider {
        float value, precision, defaultValue, defaultPrecision, minValue, maxValue, lastValueDelta;
        boolean constrained = false;
        float sliderRevealAnimationStarted = -SLIDER_REVEAL_DURATION;

        SliderFloat(Group parent, String name, float defaultValue, float precision, boolean constrained, float min,
                    float max) {
            super(parent, name);
            this.value = defaultValue;
            this.defaultValue = defaultValue;
            this.precision = precision;
            this.defaultPrecision = precision;
            if (constrained) {
                this.constrained = true;
                minValue = min;
                maxValue = max;
            } else {
                autoDetectConstraints(name);
            }
        }

        private void autoDetectConstraints(String name) {
            if (name.equals("fill") || name.equals("stroke")) {
                this.constrained = true;
                minValue = 0;
                maxValue = 255;
            } else if (name.equals("count") || name.equals("size")) {
                this.constrained = true;
                minValue = 0;
                maxValue = Float.MAX_VALUE;
            } else if (name.equals("drag")) {
                this.constrained = true;
                minValue = 0;
                maxValue = 1;
            }
        }

        void handleKeyboardInput() {
            if (previousActions.contains(ACTION_PRECISION_ZOOM_OUT) && precision < PRECISION_MAXIMUM) {
                precision *= 10f;
                pushCurrentStateToUndoNaturally();
            }
            if (previousActions.contains(ACTION_PRECISION_ZOOM_IN) && precision > PRECISION_MINIMUM) {
                precision *= .1f;
                pushCurrentStateToUndoNaturally();
            }
            if (overlayOwner.equals(this) && actions.contains(ACTION_RESET)) {
                pushCurrentStateToUndoNaturally();
                reset();
            }
        }

        void reset() {
            precision = defaultPrecision;
            value = defaultValue;
        }

        String getState() {
            return super.getState() + value + INNER_SEPARATOR + precision;
        }

        void setState(String newState) {
            String[] split = newState.split(INNER_SEPARATOR);
            value = Float.parseFloat(split[2]);
            precision = Float.parseFloat(split[3]);
        }

        void onOverlayShown() {
            if (!overlayVisible || !horizontalOverlayVisible) {
                sliderRevealAnimationStarted = frameCount;
            }
            horizontalOverlayVisible = true;
            verticalOverlayVisible = false;
            pickerOverlayVisible = false;
            zOverlayVisible = false;
        }

        boolean canHaveOverlay() {
            return true;
        }

        void updateOverlay() {
            float valueDelta = updateInfiniteSlider(precision, width, true, false, false);
            recordStateForUndo();
            value += valueDelta;
            lastValueDelta = valueDelta;
            if (constrained) {
                value = constrain(value, minValue, maxValue);
            }
            float revealAnimation = easedAnimation(sliderRevealAnimationStarted - SLIDER_REVEAL_START_SKIP,
                    SLIDER_REVEAL_DURATION,
                    SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(width * .5f, height - cell, width, sliderHeight, precision, value,
                    revealAnimation, true, true);
        }

    }

    class SliderXY extends Slider {
        protected float deltaX;
        PVector value = new PVector();
        PVector defaultValue = new PVector();
        float precision, defaultPrecision;
        float horizontalRevealAnimationStarted = -SLIDER_REVEAL_DURATION;
        float verticalRevealAnimationStarted = -SLIDER_REVEAL_DURATION;
        float interactionBufferMultiplier = 2.5f;
        float deltaY;

        SliderXY(Group currentGroup, String name, float defaultX, float defaultY, float precision) {
            super(currentGroup, name);
            this.precision = precision;
            this.defaultPrecision = precision;
            value.x = defaultX;
            value.y = defaultY;
            defaultValue.x = defaultX;
            defaultValue.y = defaultY;
        }

        String getState() {
            return super.getState() + precision + INNER_SEPARATOR + value.x + INNER_SEPARATOR + value.y;
        }

        void setState(String newState) {
            String[] xyz = newState.split(INNER_SEPARATOR);
            precision = Float.parseFloat(xyz[2]);
            value.x = Float.parseFloat(xyz[3]);
            value.y = Float.parseFloat(xyz[4]);
        }

        boolean canHaveOverlay() {
            return true;
        }

        void onOverlayShown() {
            if (!overlayVisible || !horizontalOverlayVisible) {
                horizontalRevealAnimationStarted = frameCount;
            }
            if (!overlayVisible || !verticalOverlayVisible) {
                verticalRevealAnimationStarted = frameCount;
            }
            horizontalOverlayVisible = true;
            verticalOverlayVisible = true;
            pickerOverlayVisible = false;
            zOverlayVisible = false;
        }

        void updateOverlay() {
            recordStateForUndo();
            updateXYSliders();
            lockSlidersOnMouseOver();
            value.x += deltaX;
            value.y += deltaY;
        }

        protected void lockSlidersOnMouseOver() {
            if (keyboardActive) {
                return;
            }
            if (isMouseOverXSlider()) {
                deltaY = 0;
            } else if (isMouseOverYSlider()) {
                deltaX = 0;
            }
        }

        protected boolean isMouseOverXSlider() {
            return isMouseOver(0, height - cell * interactionBufferMultiplier, width, sliderHeight);
        }

        protected boolean isMouseOverYSlider() {
            return isMouseOver(width - cell * interactionBufferMultiplier, 0, sliderHeight, height);
        }


        void updateXYSliders() {
            deltaX = updateFullHorizontalSlider(0, height - cell, width, sliderHeight, value.x, precision,
                    horizontalRevealAnimationStarted, false);
            deltaY = updateFullHeightVerticalSlider(0, width - cell, height, sliderHeight, value.y, precision,
                    verticalRevealAnimationStarted, false);
        }

        boolean keyboardInteractionJustStarted() {
            boolean wasKeyboardActive = previousActions.contains(ACTION_LEFT) ||
                    previousActions.contains(ACTION_RIGHT) ||
                    previousActions.contains(ACTION_UP) ||
                    previousActions.contains(ACTION_DOWN);
            boolean isKeyboardActive = actions.contains(ACTION_LEFT) ||
                    actions.contains(ACTION_RIGHT) ||
                    actions.contains(ACTION_UP) ||
                    actions.contains(ACTION_DOWN);
            return !wasKeyboardActive && isKeyboardActive;
        }

        void handleKeyboardInput() {
            if (previousActions.contains(ACTION_PRECISION_ZOOM_IN) && precision > PRECISION_MINIMUM) {
                precision *= .1f;
                pushCurrentStateToUndoNaturally();
            }
            if (previousActions.contains(ACTION_PRECISION_ZOOM_OUT) && precision < PRECISION_MAXIMUM) {
                precision *= 10f;
                pushCurrentStateToUndoNaturally();
            }
            if (overlayOwner.equals(this) && actions.contains(ACTION_RESET)) {
                pushCurrentStateToUndoNaturally();
                reset();
            }
        }

        void reset() {
            precision = defaultPrecision;
            value.x = defaultValue.x;
            value.y = defaultValue.y;
            value.z = defaultValue.z;
        }
    }

    class SliderXYZ extends SliderXY {
        private float zRevealAnimationStarted = -SLIDER_REVEAL_DURATION;
        private float deltaZ;

        SliderXYZ(Group currentGroup, String name, float defaultX, float defaultY, float defaultZ, float precision) {
            super(currentGroup, name, defaultX, defaultY, precision);
            this.defaultValue.z = defaultZ;
            this.value.z = defaultZ;
        }

        void update() {
            super.update();
        }

        void handleKeyboardInput() {
            super.handleKeyboardInput();
        }

        String getState() {
            return super.getState() + INNER_SEPARATOR + value.z;
        }

        void setState(String newState) {
            super.setState(newState);
            value.z = Float.parseFloat(newState.split(INNER_SEPARATOR)[5]);
        }

        void updateOverlay() {
            recordStateForUndo();
            deltaZ = updateInfiniteSlider(precision, height * .5f, false, true, true);
            lockSlidersOnMouseOver();
            value.x += deltaX;
            value.y += deltaY;
            value.z += deltaZ;
            float zAnimation = easedAnimation(zRevealAnimationStarted, SLIDER_REVEAL_DURATION, SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(height - height * .2f, width - sliderHeight * 2, height / 3f,
                    sliderHeight * .8f, precision, value.z, zAnimation, false, false);
            super.updateXYSliders();
        }

        private boolean isMouseOverZSlider() {
            stroke(1);
            return isMouseOver(width - sliderHeight * 4, 0, sliderHeight * 3, height * .4f);
        }

        protected void lockSlidersOnMouseOver() {
            if (isMouseOverXSlider()) {
                deltaY = 0;
                deltaZ = 0;
            }
            if (isMouseOverYSlider()) {
                deltaX = 0;
                deltaZ = 0;
            }
            if (isMouseOverZSlider()) {
                deltaX = 0;
                deltaY = 0;
            } else if (!(actions.contains(ACTION_LEFT) ||
                    actions.contains(ACTION_UP) ||
                    actions.contains(ACTION_RIGHT) ||
                    actions.contains(ACTION_DOWN))) {
                deltaZ = 0;
            }
        }


        void onOverlayShown() {
            if (!overlayVisible || !horizontalOverlayVisible) {
                horizontalRevealAnimationStarted = frameCount;
            }
            if (!overlayVisible || !verticalOverlayVisible) {
                verticalRevealAnimationStarted = frameCount;
            }
            if (!overlayVisible || !zOverlayVisible) {
                zRevealAnimationStarted = frameCount;
            }
            horizontalOverlayVisible = true;
            verticalOverlayVisible = true;
            pickerOverlayVisible = false;
            zOverlayVisible = true;
        }

    }

    @SuppressWarnings("SuspiciousNameCombination")
    class ColorPicker extends Slider {
        float hue, sat, br, alpha, defaultHue, defaultSat, defaultBr, defaultAlpha;
        float pickerRevealStarted = -PICKER_REVEAL_DURATION;
        float huePrecision = .5f;
        float alphaPrecision = 1;
        private boolean brightnessLocked, saturationLocked;
        private boolean satChanged, brChanged;

        ColorPicker(Group currentGroup, String name, float hue, float sat, float br, float alpha) {
            super(currentGroup, name);
            this.hue = hue;
            this.sat = sat;
            this.br = br;
            this.alpha = alpha;
            this.defaultHue = hue;
            this.defaultSat = sat;
            this.defaultBr = br;
            this.defaultAlpha = alpha;
        }

        void handleKeyboardInput() {
            if (overlayOwner != null && overlayOwner.equals(this) && actions.contains(ACTION_RESET)) {
                reset();
            }
            if (alphaPrecision > ALPHA_PRECISION_MINIMUM && previousActions.contains(ACTION_PRECISION_ZOOM_IN)) {
                alphaPrecision *= .1f;
                pushCurrentStateToUndoNaturally();
            }
            if (alphaPrecision < ALPHA_PRECISION_MAXIMUM && previousActions.contains(ACTION_PRECISION_ZOOM_OUT)) {
                alphaPrecision *= 10;
                pushCurrentStateToUndoNaturally();
            }
            if (previousActions.contains(ACTION_CONTROL)) {
                satChanged = true;
                if (actions.contains(ACTION_UP)) {
                    sat -= .01f;
                } else if (actions.contains(ACTION_DOWN)) {
                    sat += .01f;
                }
            }
            if (previousActions.contains(ACTION_ALT)) {
                brChanged = true;
                if (actions.contains(ACTION_UP)) {
                    br -= .01f;
                } else if (actions.contains(ACTION_DOWN)) {
                    br += .01f;
                }
            }
            enforceConstraints();
        }

        void reset() {
            hue = defaultHue;
            sat = defaultSat;
            br = defaultBr;
            alpha = defaultAlpha;
        }

        String getState() {
            return super.getState() + hue + INNER_SEPARATOR + sat + INNER_SEPARATOR + br + INNER_SEPARATOR + alpha + INNER_SEPARATOR + alphaPrecision;
        }

        void setState(String newState) {
            String[] hsba = newState.split(INNER_SEPARATOR);
            hue = Float.parseFloat(hsba[2]);
            sat = Float.parseFloat(hsba[3]);
            br = Float.parseFloat(hsba[4]);
            alpha = Float.parseFloat(hsba[5]);
            alphaPrecision = Float.parseFloat(hsba[6]);
        }

        int value() {
            pushStyle();
            colorMode(HSB, 1, 1, 1, 1);
            int result = color(hue, sat, br, alpha);
            popStyle();
            return result;
        }

        void displayOnTray(float x, float y) {
            pushStyle();
            stroke(GRAYSCALE_TEXT_DARK);
            strokeWeight(1);
            fill(value());
            rectMode(CENTER);
            rect(x - previewTrayBoxMargin - previewTrayBoxWidth,
                    y - textSize * .5f, previewTrayBoxWidth, previewTrayBoxWidth);
            popStyle();
            super.displayOnTray(x, y);
        }

        boolean canHaveOverlay() {
            return true;
        }

        void onOverlayShown() {
            if (!pickerOverlayVisible) {
                pickerRevealStarted = frameCount;
            }
            pickerOverlayVisible = true;
            horizontalOverlayVisible = false;
            verticalOverlayVisible = false;
            zOverlayVisible = false;
        }

        void updateOverlay() {
            if (mouseJustReleased()) {
                brightnessLocked = false;
                saturationLocked = false;
            }
            recordStateForUndo();
            pushStyle();
            colorMode(HSB, 1, 1, 1, 1);
            float revealAnimation = easedAnimation(pickerRevealStarted - PICKER_REVEAL_START_SKIP,
                    PICKER_REVEAL_DURATION, PICKER_REVEAL_EASING);
            int tinySliderCount = 2;
            float tinySliderMarginCellFraction = .2f;
            float tinySliderWidth = cell * 1.5f * (1 + tinySliderMarginCellFraction);
            float x = width - tinySliderWidth * tinySliderCount * (1 + tinySliderMarginCellFraction)
                    + tinySliderMarginCellFraction * tinySliderCount;
            float h = cell * 4;
            float tinySliderTopY =
                    height - sliderHeight * .5f - cell * tinySliderMarginCellFraction - h * revealAnimation;
            float lastSat = sat;
            sat = updateTinySlider(x, tinySliderTopY, tinySliderWidth, h, brightnessLocked, SATURATION);
            if (sat != lastSat && !saturationLocked) {
                brightnessLocked = true;
            }
            if (saturationLocked) {
                sat = lastSat;
            }

            displayTinySlider(x, tinySliderTopY, tinySliderWidth, cell * 4, sat, SATURATION,
                    brightnessLocked);

            x += tinySliderWidth * 1.2f;
            float lastBr = br;
            br = updateTinySlider(x, tinySliderTopY, tinySliderWidth, h, saturationLocked, BRIGHTNESS);
            if (br != lastBr && !brightnessLocked) {
                saturationLocked = true;
            }
            if (brightnessLocked) {
                br = lastBr;
            }

            displayTinySlider(x, tinySliderTopY, tinySliderWidth, cell * 4, br, BRIGHTNESS, saturationLocked);
            displayInfiniteSliderCenterMode(height - height / 4f, width - sliderHeight * .5f, height / 2f, sliderHeight,
                    alphaPrecision, alpha, revealAnimation, false, false);
            fill(GRAYSCALE_TEXT_DARK);
            textAlign(CENTER, CENTER);
            textSize(textSize);
            text("alpha", width - sliderHeight * .5f, 15);
            float alphaDelta = updateInfiniteSlider(alphaPrecision, height, false, false, false);
            boolean isMouseInTopHalf = isMouseOver(0, 0, width, height / 2);
            if (!satChanged && !brChanged && (keyboardActive || isMouseInTopHalf)) {
                alpha += alphaDelta;
            }

            displayHueSlider(width, sliderHeight, revealAnimation);
            float hueDelta = updateInfiniteSlider(huePrecision, width, true, false, false);
            if (!satChanged && !brChanged && (keyboardActive || !isMouseInTopHalf)) {
                hue += hueDelta;
            }
            satChanged = false;
            brChanged = false;
            enforceConstraints();
            displayValueRectangle(sliderHeight);
            popStyle();
        }

        private void displayHueSlider(float w, float h, float revealAnimation) {
            displayHueStripCornerMode(height + cell - h * revealAnimation, h * .5f, true, revealAnimation);
            displayHueStripCornerMode(height + cell - h * .5f * revealAnimation, h * .5f, false, revealAnimation);
        }

        private void displayHueStripCornerMode(float y, float h, boolean top, float revealAnimation) {
            beginShape(TRIANGLE_STRIP);
            noStroke();
            int detail = floor(width * .3f);
            for (int i = 0; i < detail; i++) {
                float iNorm = norm(i, 0, detail - 1);
                float x = iNorm * width;
                if (abs(.5f - iNorm) * 2 > revealAnimation) {
                    continue;
                }
                float iHue = hueModulo(hue - .5f + iNorm);
                int iColor = getColorAt(iHue, HUE);
                fill(iColor);
                vertex(x, y);
                vertex(x, y + h);
            }
            endShape();
        }

        private void displayValueRectangle(float hueSliderHeight) {
            float x = width * .5f;
            float y = height - hueSliderHeight - cell * 3f;
            noStroke();
            fill(value());
            rectMode(CENTER);
            rect(x, y, cell * 3, cell * 3);
        }

        private float updateTinySlider(float x, float topY, float w, float h, boolean forceActive, String type) {
            float interactionBuffer = cell;
            if (forceActive || (mousePressed && isMouseOver(x, topY - interactionBuffer, w,
                    h + interactionBuffer * 1.2f))) {
                float newValue = constrain(map(mouseY, topY, topY + h, 0, 1), 0, 1);
                setValue(newValue, type);
            }
            return getValue(type);
        }

        private void displayTinySlider(float x, float topY, float w, float h, float value, String type,
                                       boolean mouseOver) {
            beginShape(TRIANGLE_STRIP);
            noStroke();
            int detail = floor(h * .1f);
            for (int i = 0; i < detail; i++) {
                float iNorm = norm(i, 0, detail - 1);
                float y = topY + h * iNorm;
                fill(getColorAt(iNorm, type));
                vertex(x, y);
                vertex(x + w, y);
            }
            endShape();
            float valueY = topY + h * value;
            strokeWeight(2);
            stroke((type.equals(SATURATION) && satChanged) ||
                    (type.equals(BRIGHTNESS) && brChanged) || mouseOver ?
                    GRAYSCALE_TEXT_SELECTED : GRAYSCALE_TEXT_DARK);
            line(x - 2, valueY, x + w + 2, valueY);
        }

        private int getColorAt(float value, String type) {
            if (type.equals(HUE)) {
                return color(value, sat, br, alpha);
            }
            if (type.equals(SATURATION)) {
                return color(hue, value, br, alpha);
            }
            if (type.equals(BRIGHTNESS)) {
                return color(hue, sat, value, alpha);
            }
            return 0;
        }


        private float getValue(String type) {
            if (type.equals(SATURATION)) {
                return sat;
            }
            if (type.equals(BRIGHTNESS)) {
                return br;
            }
            return 0;
        }

        private void setValue(float newValue, String type) {
            if (type.equals(SATURATION)) {
                sat = newValue;
                satChanged = true;
            }
            if (type.equals(BRIGHTNESS)) {
                br = newValue;
                brChanged = true;
            }
        }

        private void enforceConstraints() {
            hue = hueModulo(hue);
            sat = constrain(sat, 0, 1);
            br = constrain(br, 0, 1);
            alpha = constrain(alpha, 0, 1);
        }

        private float hueModulo(float hue) {
            while (hue < 0) {
                hue += 1;
            }
            hue %= 1;
            return hue;
        }
    }
}
