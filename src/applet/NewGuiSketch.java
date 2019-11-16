package applet;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"InnerClassMayBeStatic", "SameParameterValue", "FieldCanBeLocal", "BooleanMethodIsAlwaysInverted",
        "unused", "ConstantConditions",
        "WeakerAccess"})
public abstract class NewGuiSketch extends PApplet {
    //TODO:
    // ----- Operation JUICE -----
    // scrolling down to allow unlimited group and element count

    private static final String REGEX_SEPARATOR = "_!_";
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
    private static final float BACKGROUND_ALPHA = .5f;
    private static final float GRAYSCALE_GRID = .3f;
    private static final float GRAYSCALE_TEXT_DARK = .6f;
    private static final float GRAYSCALE_TEXT_SELECTED = 1;
    private static final float PRECISION_MAXIMUM = 10000;
    private static final float PRECISION_MINIMUM = .1f;
    private static final float ALPHA_PRECISION_MINIMUM = .005f;
    private static final float ALPHA_PRECISION_MAXIMUM = .5f;
    private static final int UNDERLINE_TRAY_ANIMATION_DURATION = 10;
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
    private static final String SATURATION = "saturation";
    private static final String BRIGHTNESS = "brightness";
    private static final String HUE = "hue";
    protected String captureDir;
    protected String captureFilename;
    protected String id = regenId();
    protected int frameRecordingEnds;
    protected int recordingFrames = 360; // assuming t += radians(1) per frame for a perfect loop
    protected float t;
    protected boolean mousePressedOutsideGui = false;
    private int underlineTrayAnimationStarted = -UNDERLINE_TRAY_ANIMATION_DURATION;
    private ArrayList<ArrayList<String>> undoStack = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> redoStack = new ArrayList<ArrayList<String>>();
    private ArrayList<Group> groups = new ArrayList<Group>();
    private Group currentGroup = null; // do not assign to nor read directly!
    private ArrayList<Key> keyboardKeys = new ArrayList<Key>();
    private ArrayList<Key> keyboardKeysToRemove = new ArrayList<Key>();
    private ArrayList<String> actions = new ArrayList<String>();
    private ArrayList<String> previousActions = new ArrayList<String>();
    private boolean pMousePressed = false;
    private int keyboardSelectionIndex = 0;
    private boolean keyboardSelectionActive = false;
    private boolean onWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private float textSize = onWindows ? 24 : 48;
    private float cell = onWindows ? 40 : 80;
    private float minimumTrayWidthCellCount = 6;
    private float minimumTrayWidth = cell * minimumTrayWidthCellCount;
    private float trayWidth = minimumTrayWidth;
    private float sliderHeight = cell * 2;
    private boolean trayVisible = true;
    private boolean overlayVisible = false;
    private boolean horizontalOverlayVisible;
    private boolean verticalOverlayVisible;
    private boolean pickerOverlayVisible;
    private Element overlayOwner = null; // do not assign directly!
    private float MENU_ROTATION_DURATION = 10;
    private float MENU_ROTATION_EASING = 3;
    private float undoRotationStarted = -MENU_ROTATION_DURATION;
    private float redoRotationStarted = -MENU_ROTATION_DURATION;
    private float undoHideRotationStarted = -MENU_ROTATION_DURATION;

    // INTERFACE
    protected float slider(String name) {
        return slider(name, 0);
    }

    protected float slider(String name, float defaultValue) {
        return slider(name, defaultValue, numberOfDigitsInFlooredNumber(defaultValue));
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
            SliderFloat newElement = new SliderFloat(currentGroup, name, defaultValue, precision, constrained, min,
                    max);
            currentGroup.elements.add(newElement);
        }
        SliderFloat slider = (SliderFloat) findElement(name, currentGroup.name);
        assert slider != null;
        return slider.value;
    }

    int numberOfDigitsInFlooredNumber(float inputNumber) {
        return String.valueOf(floor(inputNumber)).length();
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

    protected int picker(String name) {
        return picker(name, 0, 0, 0.1f, 1);
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

    protected boolean button(String name) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(name, currentGroup.name)) {
            Button newElement = new Button(currentGroup, name);
            currentGroup.elements.add(newElement);
        }
        Button button = (Button) findElement(name, currentGroup.name);
        return button.value;
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
        return toggle.state;
    }

    protected void gui() {
        gui(true);
    }

    protected void gui(boolean defaultVisibility) {
        t += radians(1);
        if (frameCount == 1) {
            firstFrameSetup(defaultVisibility);
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
    }

    private void updateMouseState() {
        mousePressedOutsideGui = mousePressed && isMouseOutsideGui();
    }

    private void firstFrameSetup(boolean defaultVisibility) {
        trayVisible = defaultVisibility;
        textSize(textSize * 2);
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

    public ArrayList<PImage> loadImages(String folderPath) {
        ArrayList<PImage> images = new ArrayList<PImage>();
        try {
            List<File> filesInFolder = Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            for (File file : filesInFolder) {
                if (file.isDirectory() || (!file.getName().contains(".jpg") && !file.getName().contains(".png"))) {
                    continue;
                }
                PImage frame = loadImage(Paths.get(folderPath).toAbsolutePath() + "\\" + file.getName());
                images.add(frame);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return images;
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
        float rotation = ease(constrain(norm(frameCount, undoHideRotationStarted,
                undoHideRotationStarted + MENU_ROTATION_DURATION), 0, 1), MENU_ROTATION_EASING);
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
            keyboardSelectionIndex = 1;
            pushCurrentStateToRedo();
            popUndoToCurrentState();
            undoRotationStarted = frameCount;
        }
        float rotation = ease(constrain(norm(frameCount, undoRotationStarted,
                undoRotationStarted + MENU_ROTATION_DURATION), 0, 1), MENU_ROTATION_EASING);
        displayMenuButton(x, y, w, h, rotation * TWO_PI, false, MENU_BUTTON_UNDO, undoStack.size());
    }

    private void updateMenuButtonRedo(float x, float y, float w, float h) {
        boolean canRedo = redoStack.size() > 0;
        if (canRedo && (activated(MENU_BUTTON_REDO, x, y, w, h) || actions.contains(ACTION_REDO))) {
            keyboardSelectionIndex = 2;
            pushCurrentStateToUndoManually();
            popRedoToCurrentState();
            redoRotationStarted = frameCount;
        }
        float rotation = ease(constrain(norm(frameCount, redoRotationStarted,
                redoRotationStarted + MENU_ROTATION_DURATION), 0, 1), MENU_ROTATION_EASING);
        displayMenuButton(x, y, w, h, rotation * TWO_PI, true, MENU_BUTTON_REDO, redoStack.size());
    }

    private void displayMenuButton(float x, float y, float w, float h, float rotation,
                                   boolean direction, String menuButtonType, int number) {
        textSize(textSize);
        textAlign(CENTER, CENTER);
        float grayscale = (keyboardSelected(menuButtonType) || isMouseOver(x, y, w, h)) ?
                GRAYSCALE_TEXT_SELECTED : GRAYSCALE_TEXT_DARK;
        fill(grayscale);
        pushMatrix();
        translate(x + w * .5f, y + h * .5f);
        text(number, 0, 0);
        rotate(PI + (direction ? rotation : -rotation));
        float margin = 0;
        noFill();
        stroke(grayscale);
        strokeWeight(2);
        arc(0, 0, w * .7f, h * .7f, margin, PI - margin);
        fill(grayscale);
        stroke(grayscale);
        beginShape();
        vertex((direction ? -1 : 1) * w * .28f, h * .1f);
        vertex((direction ? -1 : 1) * w * .35f, 0);
        vertex((direction ? -1 : 1) * w * .4f, h * .1f);
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
        for (Group group : groups) {
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
                    updateElement(group, el, y);
                    if (trayVisible) {
                        displayElement(group, el, x, y);
                    }
                }
                x -= cell * .5f;
            }
            y += cell;
        }
    }

    private void updateElement(Group group, Element el, float y) {
        el.update();
        if (activated(group.name + el.name, 0, y - cell, trayWidth, cell)) {
            if (!el.canHaveOverlay()) {
                el.onActivationWithoutOverlay(0, y - cell, trayWidth, cell);
                return;
            }
            if (!overlayVisible) {
                setOverlayOwner(el);
            } else if (overlayVisible && !el.equals(overlayOwner)) {
                setOverlayOwner(el);
            } else if (overlayVisible && el.equals(overlayOwner)) {
                overlayVisible = false;
            }
        }
    }

    private void displayElement(Group group, Element el, float x, float y) {
        float grayScale = keyboardSelected(group.name + el.name) ||
                isMouseOver(0, y - cell, trayWidth, cell) ? GRAYSCALE_TEXT_SELECTED : GRAYSCALE_TEXT_DARK;
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
        if (!keyboardSelectionActive) {
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

    private int keyboardSelectableItemCount() {
        int elementCount = 0;
        for (Group group : groups) {
            elementCount += group.elements.size();
        }
        return MENU_BUTTON_COUNT + groups.size() + elementCount;
    }

    public void mousePressed() {
        if (!upAndDownArrowsControlOverlay()) {
            keyboardSelectionActive = false;
        }
    }

    public void keyPressed() {
        keyboardSelectionActive = true;
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
        for (Key kk : keyboardKeys) {
            if (kk.coded) {
                if (kk.character == UP) {
                    actions.add(ACTION_UP);
                    if (!upAndDownArrowsControlOverlay() && kk.repeatCheck()) {
                        if (keyboardSelectionIndex == MENU_BUTTON_COUNT) {
                            keyboardSelectionIndex = 0;
                        } else {
                            keyboardSelectionIndex -= hiddenElementCount(false);
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
                            keyboardSelectionIndex += hiddenElementCount(true);
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

    private Group getCurrentGroup() {
        if (currentGroup == null) {
            Group anonymous = new Group(this.getClass().getSimpleName());
            groups.add(anonymous);
            currentGroup = anonymous;
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
        setState(undoStack.remove(undoStack.size() - 1));
    }

    private void popRedoToCurrentState() {
        if (redoStack.size() == 0) {
            throw new IllegalStateException("nothing to pop in redo stack!");
        }
        setState(redoStack.remove(redoStack.size() - 1));
    }

    private void setState(ArrayList<String> statesToSet) {
        for (String elementState : statesToSet) {
            String[] splitState = elementState.split(REGEX_SEPARATOR);
            Element el = findElement(splitState[1], splitState[0]);
            el.setState(elementState);
        }
    }

    private ArrayList<String> getGuiState() {
        ArrayList<String> state = new ArrayList<String>();
        for (Group group : groups) {
            for (Element el : group.elements) {
                state.add(el.getState());
            }
        }
        return state;
    }

    private float easedAnimation(float startFrame, float duration, float easingFactor) {
        float revealAnimationNorm = constrain(norm(frameCount, startFrame,
                startFrame + duration), 0, 1);
        return ease(revealAnimationNorm, easingFactor);
    }

    private void displayArrow(float arrowHeight) {
        //TODO arrow
        beginShape();
        vertex(-arrowHeight * .25f, 0);
        vertex(0, arrowHeight * .5f);
        vertex(0, -arrowHeight * .5f);
        endShape();
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
            justPressed = true;
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

        public void update(float y) {
            if (activated(name, 0, y - cell, trayWidth, cell)) {
                expanded = !expanded;
            }
        }

        public void displayInTray(float x, float y) {
            fill((keyboardSelected(name) || isMouseOver(0, y - cell, trayWidth, cell)) ?
                    GRAYSCALE_TEXT_SELECTED : GRAYSCALE_TEXT_DARK);
            textAlign(LEFT, BOTTOM);
            textSize(textSize);
            text(name, x, y);
        }
    }

    abstract class Element {
        Group parent;
        String name;

        Element(Group parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        String getState() {
            return parent.name + REGEX_SEPARATOR + name + REGEX_SEPARATOR;
        }

        void setState(String newState) {

        }

        abstract boolean canHaveOverlay();

        void update() {

        }

        void updateOverlay() {

        }

        void onOverlayShown() {

        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {

        }

        void displayOnTray(float x, float y) {
            textAlign(LEFT, BOTTOM);
            textSize(textSize);
            if (overlayVisible && this.equals(overlayOwner)) {
                underlineAnimation(underlineTrayAnimationStarted, UNDERLINE_TRAY_ANIMATION_DURATION, x, y,
                        true);
            }
            text(name, x, y);
        }

        float trayTextWidth() {
            return textWidth(name);
        }

        void underlineAnimation(int startFrame, int duration, float x, float y, boolean stayExtended) {
            float fullWidth = textWidth(name);
            float animation = constrain(norm(frameCount, startFrame, startFrame + duration), 0, 1);
            float animationEased = ease(animation, UNDERLINE_TRAY_ANIMATION_EASING);
            if (!stayExtended && animation == 1) {
                animation = 0;
            }
            float w = fullWidth * animation;
            float centerX = x + fullWidth * .5f;
            strokeWeight(2);
            line(centerX - w * .5f, y, centerX + w * .5f, y);
        }

        void handleKeyboardInput() {
        }

        abstract void reset();
    }

    class Radio extends Element {
        int valueIndex = 0;
        ArrayList<String> options = new ArrayList<String>();
        int indexWhenOverlayShown = 0;

        Radio(Group parent, String name, String[] options) {
            super(parent, name);
            this.options.add(name);
            this.options.addAll(Arrays.asList(options));
        }

        String getState() {
            StringBuilder state = new StringBuilder(super.getState() + valueIndex);
            for (String option : options) {
                state.append(REGEX_SEPARATOR);
                state.append(option);
            }
            return state.toString();
        }

        void setState(String newState) {
            valueIndex = Integer.parseInt(newState.split(REGEX_SEPARATOR)[2]);
        }

        protected void reset() {
            valueIndex = 0;
        }

        boolean canHaveOverlay() {
            return false;
        }

        void onOverlayShown() {
            indexWhenOverlayShown = valueIndex;
        }

        boolean stateChangedWhileOverlayWasShown() {
            return valueIndex == indexWhenOverlayShown;
        }

        String value() {
            return options.get(valueIndex);
        }

        void displayOnTray(float x, float y) {
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
                if (i != valueIndex) {
                    float strikethroughY = y - textSize * .5f;
                    strokeWeight(2);
                    line(optionX, strikethroughY, optionX + textWidthWithoutSeparator, strikethroughY);
                }
                text(option, optionX, y);
                optionX += textWidth(option);
            }
            popStyle();
        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {
            pushCurrentStateToUndoNaturally();
            valueIndex++;
            if (valueIndex >= options.size()) {
                valueIndex = 0;
            }
        }
    }

    class Button extends Element {
        boolean value;
        int activationAnimationDuration = 10;
        int activationAnimationStarted = -activationAnimationDuration;

        Button(Group parent, String name) {
            super(parent, name);
        }

        boolean canHaveOverlay() {
            return false;
        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {
            value = true;
        }

        void displayOnTray(float x, float y) {
            if (value) {
                activationAnimationStarted = frameCount;
            }
            super.displayOnTray(x, y);
            underlineAnimation(activationAnimationStarted, activationAnimationDuration, x, y, false);
        }

        @Override
        void reset() {

        }

        void update() {
            value = false;
        }
    }

    class Toggle extends Element {
        boolean state, defaultState;

        Toggle(Group parent, String name, boolean defaultState) {
            super(parent, name);
            this.defaultState = defaultState;
            this.state = defaultState;
        }

        String getState() {
            return super.getState() + state;
        }

        void setState(String newState) {
            this.state = Boolean.parseBoolean(newState.split(REGEX_SEPARATOR)[2]);
        }

        boolean canHaveOverlay() {
            return false;
        }

        void displayOnTray(float x, float y) {
            if (state) {
                // TODO display this differently, it clashes with the slider overlay indicator
                strokeWeight(2);
                line(x, y, x + textWidth(name), y);
            }
            text(name, x, y);
            super.displayOnTray(x, y);
        }

        void reset() {
            state = defaultState;
        }

        void update() {
            if (overlayOwner != null && overlayOwner.equals(this) && actions.contains(ACTION_RESET)) {
                reset();
            }
        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {
            pushCurrentStateToUndoNaturally();
            state = !state;
        }
    }

    abstract class Slider extends Element {
        Slider(Group parent, String name) {
            super(parent, name);
        }

        @Override
        void reset() {

        }

        float updateInfiniteSlider(float precision, float sliderWidth, boolean horizontal, boolean reversed) {
            if (mousePressed && isMouseOutsideGui()) {
                float screenSpaceDelta = horizontal ? (pmouseX - mouseX) : (pmouseY - mouseY);
                if (reversed) {
                    screenSpaceDelta *= -1;
                }
                float valueSpaceDelta = screenDistanceToValueDistance(screenSpaceDelta, precision, sliderWidth);
                if (valueSpaceDelta != 0) {
                    return valueSpaceDelta;
                }
            }
            if (actions.contains(ACTION_LEFT) && horizontal) {
                return screenDistanceToValueDistance(-3, precision, sliderWidth);
            }
            if (actions.contains(ACTION_RIGHT) && horizontal) {
                return screenDistanceToValueDistance(3, precision, sliderWidth);
            }
            if (actions.contains(ACTION_UP) && !horizontal) {
                return screenDistanceToValueDistance(-3, precision, sliderWidth);
            }
            if (actions.contains(ACTION_DOWN) && !horizontal) {
                return screenDistanceToValueDistance(3, precision, sliderWidth);
            }
            return 0;
        }

        float screenDistanceToValueDistance(float screenSpaceDelta, float precision, float sliderWidth) {
            float valueToScreenRatio = precision / sliderWidth;
            return screenSpaceDelta * valueToScreenRatio;
        }

        void displayInfiniteSliderCenterMode(float x, float y, float w, float h, float precision, float value,
                                             boolean horizontal, float revealAnimation, boolean cutout) {
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
            textSize(textSize * 2);
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
            } else if (name.equals("fill") || name.equals("stroke")) {
                this.constrained = true;
                minValue = 0;
                maxValue = 255;
            } else {
                minValue = -Float.MAX_VALUE;
                maxValue = Float.MAX_VALUE;
            }
        }

        void handleKeyboardInput() {
            if (previousActions.contains(ACTION_PRECISION_ZOOM_OUT) && precision > PRECISION_MINIMUM) {
                precision *= 10f;
            }
            if (previousActions.contains(ACTION_PRECISION_ZOOM_IN) && precision < PRECISION_MAXIMUM) {
                precision *= .1f;
            }
            if (overlayOwner != null && actions.contains(ACTION_RESET)) {
                pushCurrentStateToUndoNaturally();
                reset();
            }
        }

        void reset() {
            precision = defaultPrecision;
            value = defaultValue;
        }

        String getState() {
            return super.getState() + value;
        }

        void setState(String newState) {
            String[] state = newState.split(REGEX_SEPARATOR);
            value = Float.parseFloat(state[2]);
        }

        void onOverlayShown() {
            if (!overlayVisible || !horizontalOverlayVisible) {
                sliderRevealAnimationStarted = frameCount;
            }
            horizontalOverlayVisible = true;
            verticalOverlayVisible = false;
            pickerOverlayVisible = false;
        }

        boolean canHaveOverlay() {
            return true;
        }

        void updateOverlay() {
            float valueDelta = updateInfiniteSlider(precision, width, true, false);
            recordStateForUndo();
            value += valueDelta;
            lastValueDelta = valueDelta;
            if (constrained) {
                value = constrain(value, minValue, maxValue);
            }
            float revealAnimation = easedAnimation(sliderRevealAnimationStarted - SLIDER_REVEAL_START_SKIP,
                    SLIDER_REVEAL_DURATION,
                    SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(width * .5f, height - cell, width, sliderHeight, precision, value, true,
                    revealAnimation, true);
        }

    }

    class SliderXY extends Slider {
        PVector value = new PVector();
        PVector defaultValue = new PVector();
        float precision, defaultPrecision;
        float horizontalRevealAnimationStarted = -SLIDER_REVEAL_DURATION;
        float verticalRevealAnimationStarted = -SLIDER_REVEAL_DURATION;


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
            return super.getState() + value.x + REGEX_SEPARATOR + value.y;
        }

        void setState(String newState) {
            String[] xyz = newState.split(REGEX_SEPARATOR);
            value.x = Float.parseFloat(xyz[2]);
            value.y = Float.parseFloat(xyz[3]);
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
        }

        void updateOverlay() {
            recordStateForUndo();
            float deltaX = updateHorizontalSlider(0, height - cell, width, sliderHeight);
            float deltaY = updateVerticalSlider(0, width - cell, height, sliderHeight);
            float interactionBufferMultiplier = 2.5f;
            if (isMouseOver(0, height - cell * interactionBufferMultiplier, width, sliderHeight)) {
                deltaY = 0;
            } else if (isMouseOver(width - cell * interactionBufferMultiplier, 0, sliderHeight, height)) {
                deltaX = 0;
            }
            value.x += deltaX;
            value.y += deltaY;
        }

        private float updateHorizontalSlider(float x, float y, float w, float h) {
            float deltaX = updateInfiniteSlider(precision, width, true, true);
            float horizontalAnimation = easedAnimation(horizontalRevealAnimationStarted - SLIDER_REVEAL_START_SKIP,
                    SLIDER_REVEAL_DURATION, SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(x + width * .5f, y, w, h,
                    precision, value.x, true, horizontalAnimation, true);
            return deltaX;
        }

        private float updateVerticalSlider(float x, float y, float w, float h) {
            float deltaY = updateInfiniteSlider(precision, width, false, true);
            float verticalAnimation = easedAnimation(verticalRevealAnimationStarted - SLIDER_REVEAL_START_SKIP,
                    SLIDER_REVEAL_DURATION, SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(x + height * .5f, y, w, h,
                    precision, value.y, false, verticalAnimation, true);
            return deltaY;
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
            }
            if (previousActions.contains(ACTION_PRECISION_ZOOM_OUT) && precision < PRECISION_MAXIMUM) {
                precision *= 10f;
            }
            if (overlayOwner != null && actions.contains(ACTION_RESET)) {
                pushCurrentStateToUndoNaturally();
                precision = defaultPrecision;
                value.x = defaultValue.x;
                value.y = defaultValue.y;
            }
        }

        void reset() {
            precision = defaultPrecision;
            value.x = defaultValue.x;
            value.y = defaultValue.y;
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
            }
            if (alphaPrecision < ALPHA_PRECISION_MAXIMUM && previousActions.contains(ACTION_PRECISION_ZOOM_OUT)) {
                alphaPrecision *= 10;
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
            return super.getState() + hue + REGEX_SEPARATOR + sat + REGEX_SEPARATOR + br + REGEX_SEPARATOR + alpha;
        }

        void setState(String newState) {
            String[] hsba = newState.split(REGEX_SEPARATOR);
            hue = Float.parseFloat(hsba[2]);
            sat = Float.parseFloat(hsba[3]);
            br = Float.parseFloat(hsba[4]);
            alpha = Float.parseFloat(hsba[5]);
        }

        int value() {
            pushStyle();
            colorMode(HSB, 1, 1, 1, 1);
            int result = color(hue, sat, br, alpha);
            popStyle();
            return result;
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
        }


        void updateOverlay() {
            if (mouseJustReleased()) {
                brightnessLocked = false;
                saturationLocked = false;
            }
            recordStateForUndo();
            pushStyle();
            colorMode(HSB, 1, 1, 1, 1);
            float revealAnimation = ease(constrain(norm(frameCount + PICKER_REVEAL_START_SKIP, pickerRevealStarted,
                    pickerRevealStarted + PICKER_REVEAL_DURATION), 0, 1), PICKER_REVEAL_EASING);
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
            displayInfiniteSliderCenterMode(height - height / 4, width - sliderHeight * .5f, height / 2f, sliderHeight,
                    alphaPrecision, alpha, false, revealAnimation, false);
            fill(GRAYSCALE_TEXT_DARK);
            textAlign(CENTER, CENTER);
            textSize(textSize);
            text("alpha", width - sliderHeight * .5f, 15);
            float alphaDelta = updateInfiniteSlider(alphaPrecision, height, false, false);
            boolean isMouseInTopHalf = isMouseOver(0, 0, width, height / 2);
            if (!satChanged && !brChanged && (keyboardSelectionActive || isMouseInTopHalf)) {
                alpha += alphaDelta;
            }

            displayHueSlider(width, sliderHeight, revealAnimation);
            float hueDelta = updateInfiniteSlider(huePrecision, width, true, false);
            if (!satChanged && !brChanged && (keyboardSelectionActive || !isMouseInTopHalf)) {
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
