package applet;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.opengl.PShader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.currentTimeMillis;

/**
 * TODO:
 * - must have:
 * touchscreen slider precision, reset
 * try to set last known undo state to any new element
 * - nice to have:
 * record states with frame stamp before recording in realtime or well controlled slow-mo, then play them back
 * more minimalist animated juice...
 * range picker (2 floats, start and end, end > start)
 * - bugs:
 * first null group's element appears twice, once at the top and once at the bottom
 */

@SuppressWarnings({"WeakerAccess", "SameParameterValue", "unused"})
public abstract class KrabApplet extends PApplet {
    private static final String STATE_BEGIN = "STATE_BEGIN";
    private static final String STATE_END = "STATE_END";
    private static final String SEPARATOR = "§";
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
    private static final String ACTION_HIDE = "HIDE";
    private static final String ACTION_UNDO = "UNDO";
    private static final String ACTION_REDO = "REDO";
    private static final String ACTION_SAVE = "SAVE";
    private static final String ACTION_LOAD = "load";
    private static final int MENU_BUTTON_COUNT = 4;
    private static final String MENU_BUTTON_HIDE = "hide";
    private static final String MENU_BUTTON_UNDO = "undo";
    private static final String MENU_BUTTON_REDO = "redo";
    private static final String MENU_BUTTON_SAVE = "save";
    private static final String SATURATION = "saturation";
    private static final String BRIGHTNESS = "brightness";
    private static final String HUE = "hue";
    private static final float BACKGROUND_ALPHA = .9f;
    private static final float GRAYSCALE_GRID = .3f;
    private static final float GRAYSCALE_TEXT_DARK = .5f;
    private static final float GRAYSCALE_TEXT_SELECTED = 1;
    private static final float INT_PRECISION_MAXIMUM = 10000;
    private static final float INT_PRECISION_MINIMUM = 10f;
    private static final float FLOAT_PRECISION_MAXIMUM = 10000;
    private static final float FLOAT_PRECISION_MINIMUM = .1f;
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
    private static final int KEY_REPEAT_DELAY_FIRST = 300;
    private static final int KEY_REPEAT_DELAY = 30;
    private static final float MENU_ROTATION_DURATION = 20;
    private static final float MENU_ROTATION_EASING = 2;
    private static final float DESELECTION_FADEOUT_DURATION = 10;
    private static final float DESELECTION_FADEOUT_EASING = 1;
    private static final float CHECK_ANIMATION_DURATION = 10;
    private static final float CHECK_ANIMATION_EASING = 1;
    private final boolean onWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private final float textSize = onWindows ? 24 : 48;
    private final float cell = onWindows ? 40 : 80;
    private final float hideButtonWidth = cell * 2;
    private final float menuButtonSize = cell * 1.5f;
    private final float previewTrayBoxWidth = cell * .375f;
    private final float previewTrayBoxMargin = cell * .125f;
    private final float previewTrayBoxOffsetY = -cell * .025f;
    private final float minimumTrayWidth = hideButtonWidth + (MENU_BUTTON_COUNT - 1) * menuButtonSize;
    private final float sliderHeight = cell * 2;
    protected String captureDir;
    protected String id = regenIdAndCaptureDir();
    protected float t;
    protected boolean mousePressedOutsideGui = false;
    protected int frameRecordingStarted = 0;
    protected int frameRecordingDuration = 360; // assuming t += radians(1) per frame for a perfect loop
    private float trayWidthWhenExtended = minimumTrayWidth;
    private float trayWidth = minimumTrayWidth;
    private ArrayList<ArrayList<String>> undoStack = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> redoStack = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> replayStack = new ArrayList<ArrayList<String>>();
    private boolean replayStackRecordingPaused = true;
    private boolean captureScreenshot = false;
    private int screenshotsAlreadyCaptured = 0;

    private ArrayList<Group> groups = new ArrayList<Group>();
    private Group currentGroup = null; // do not assign to nor read directly!
    private ArrayList<Key> keyboardKeys = new ArrayList<Key>();
    private ArrayList<Key> keyboardKeysToRemove = new ArrayList<Key>();
    private ArrayList<String> actions = new ArrayList<String>();
    private ArrayList<String> previousActions = new ArrayList<String>();
    private boolean pMousePressed = false;
    private int keyboardSelectionIndex = MENU_BUTTON_COUNT;
    private boolean keyboardActive = true;
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
    private float hideRotationStarted = -MENU_ROTATION_DURATION;
    private float saveAnimationStarted = -MENU_ROTATION_DURATION;
    private int undoHoldDuration = 0;
    private int redoHoldDuration = 0;
    private int menuButtonHoldThreshold = 60;
    private float trayScrollOffset = 0;
    private ArrayList<Float> scrollOffsetHistory = new ArrayList<Float>();
    private ArrayList<ShaderSnapshot> snapshots = new ArrayList<ShaderSnapshot>();
    private int shaderRefreshRateInMillis = 36;

    // INTERFACE

    protected int sliderInt() {
        return floor(sliderInt("x"));
    }

    protected int sliderInt(String name) {
        return floor(sliderInt(name, 0));
    }

    protected int sliderInt(String name, int defaultValue) {
        return sliderInt(name, defaultValue, numberOfDigitsInFlooredNumber(defaultValue) * 100);
    }

    protected int sliderInt(String name, int max, boolean defaultMax) {
        return sliderInt(name, defaultMax ? 0 : max, numberOfDigitsInFlooredNumber(max) * 100);
    }

    protected int sliderInt(String name, int defaultValue, int precision) {
        return floor(slider(name, defaultValue, precision, false, -Float.MAX_VALUE, Float.MAX_VALUE, true));
    }

    protected int sliderInt(String name, int min, int max, int defaultValue) {
        return floor(slider(name, defaultValue, numberOfDigitsInFlooredNumber(max) * 100, true, min, max, true));
    }

    protected int sliderInt(String name, int defaultValue, int precision, boolean constrained, int min, int max) {
        return floor(slider(name, defaultValue, precision, constrained, min, max, true));
    }

    protected float slider() {
        return slider("x", 0);
    }

    protected float slider(String name) {
        return slider(name, 0);
    }

    protected float slider(String name, float defaultValue) {
        return slider(name, defaultValue, numberOfDigitsInFlooredNumber(defaultValue) * 10);
    }

    protected float slider(String name, float max, boolean defaultMax) {
        return slider(name, defaultMax ? 0 : max, max * .5f);
    }

    protected float slider(String name, float defaultValue, float precision) {
        return slider(name, defaultValue, precision, false, -Float.MAX_VALUE, Float.MAX_VALUE, false);
    }

    protected float slider(String name, float min, float max, float defaultValue) {
        return slider(name, defaultValue, max - min, true, min, max, false);
    }

    protected float slider(String name, float defaultValue, float precision, boolean constrained, float min,
                           float max, boolean floored) {
        Group currentGroup = getCurrentGroup();
        if (elementDoesntExist(name, currentGroup.name)) {
            SliderFloat newElement = new SliderFloat(currentGroup, name, defaultValue, precision,
                    constrained, min, max, floored);
            currentGroup.elements.add(newElement);
        }
        SliderFloat slider = (SliderFloat) findElement(name, currentGroup.name);
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
        if (elementDoesntExist(name, currentGroup.name)) {
            SliderXY newElement = new SliderXY(currentGroup, name, defaultX, defaultY, precision);
            currentGroup.elements.add(newElement);
        }
        SliderXY slider = (SliderXY) findElement(name, currentGroup.name);
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
        if (elementDoesntExist(name, currentGroup.name)) {
            SliderXYZ newElement = new SliderXYZ(currentGroup, name, x, y, z, precision);
            currentGroup.elements.add(newElement);
        }
        SliderXYZ slider = (SliderXYZ) findElement(name, currentGroup.name);
        return slider.value;
    }

    protected HSBA picker() {
        return picker("color");
    }

    protected HSBA picker(String name) {
        return picker(name, 0, 0, .5f, 1);
    }

    protected HSBA picker(String name, int grayscale) {
        return picker(name, 0, 0, grayscale);
    }

    protected HSBA picker(String name, float grayscale, float alpha) {
        return picker(name, 0, 0, grayscale, alpha);
    }

    protected HSBA picker(String name, float hue, float sat, float br) {
        return picker(name, hue, sat, br, 1);
    }

    protected HSBA picker(String name, float hue, float sat, float br, float alpha) {
        Group currentGroup = getCurrentGroup();
        if (elementDoesntExist(name, currentGroup.name)) {
            ColorPicker newElement = new ColorPicker(currentGroup, name, hue, sat, br, alpha);
            currentGroup.elements.add(newElement);
        }
        ColorPicker picker = (ColorPicker) findElement(name, currentGroup.name);
        if (picker != null) {
            return picker.getHSBA();
        }
        return new HSBA();
    }

    protected String optionsABC() {
        return options("A", "B", "C");
    }

    protected String options(String defaultValue, String... otherValues) {
        Group currentGroup = getCurrentGroup();
        if (elementDoesntExist(defaultValue, currentGroup.name)) {
            Element newElement = new Radio(currentGroup, defaultValue, otherValues);
            currentGroup.elements.add(newElement);
        }
        Radio radio = (Radio) findElement(defaultValue, currentGroup.name);
        return radio.options.get(radio.valueIndex);
    }

    protected boolean button() {
        return button("button");
    }

    protected boolean button(String name) {
        Group currentGroup = getCurrentGroup();
        if (elementDoesntExist(name, currentGroup.name)) {
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
        if (elementDoesntExist(name, currentGroup.name)) {
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
        t += radians(1 / (frameRecordingDuration / 360f));
        guiSetup(defaultVisibility);
        updateKeyboardInput();
        updateMouseState();
        pushStyle();
        pushMatrix();
        strokeCap(SQUARE);
        colorMode(HSB, 1, 1, 1, 1);
        resetMatrixInAnyRenderer();
        updateTrayBackground();
        updateMenuButtons();
        updateScrolling();
        updateGroupsAndTheirElements();
        updateFps();
        if (overlayVisible && trayVisible) {
            overlayOwner.updateOverlay();
        }
        popStyle();
        popMatrix();
        pMousePressed = mousePressed;
        if(frameCount == 1){
            trayVisible = elementCount() != 0;
        }
    }

    private void updateScrolling() {
        if (!(trayVisible && isMousePressedHere(0, 0, trayWidth, height))) {
            return;
        }
        scrollOffsetHistory.add(trayScrollOffset);
        int scrollOffsetHistorySize = 3;
        while (scrollOffsetHistory.size() > scrollOffsetHistorySize) {
            scrollOffsetHistory.remove(0);
        }
        if (abs(pmouseY - mouseY) > 2) {
            trayScrollOffset += mouseY - pmouseY;
        }
    }

    private boolean trayHasntMovedInAWhile() {
        for (Float historicalTrayOffset : scrollOffsetHistory) {
            if (historicalTrayOffset != trayScrollOffset) {
                return false;
            }
        }
        return true;
    }

    private void updateMouseState() {
        mousePressedOutsideGui = mousePressed && isMouseOutsideGui() && !overlayVisible;
    }

    private void guiSetup(boolean defaultVisibility) {
        if (frameCount == 1) {
            trayVisible = defaultVisibility;
            textSize(textSize * 2);
            registerExitHandler();
        } else if (frameCount == 3) {
            loadLastStateFromFile(true);
        }
    }

    // UTILS

    protected void alphaFade(){
        alphaFade(g);
    }

    protected void alphaFade(PGraphics pg) {
        pg.pushStyle();
        pg.colorMode(HSB, 255, 255, 255, 255);
        pg.hint(DISABLE_DEPTH_TEST);
        pg.blendMode(SUBTRACT);
        pg.noStroke();
        pg.fill(255, slider("fade to black", 0, 255, 50));
        pg.rectMode(CENTER);
        pg.rect(0, 0, width * 2, height * 2);
        pg.hint(ENABLE_DEPTH_TEST);
        pg.popStyle();
    }

    protected void alphaAdd(PGraphics pg) {
        pg.pushStyle();
        pg.colorMode(HSB, 255, 255, 255, 255);
        pg.hint(DISABLE_DEPTH_TEST);
        pg.blendMode(ADD);
        pg.noStroke();
        pg.fill(255, slider("fade to white", 0, 255, 50));
        pg.rectMode(CENTER);
        pg.rect(0, 0, width * 2, height * 2);
        pg.hint(ENABLE_DEPTH_TEST);
        pg.popStyle();
    }

    protected void colorFilter(){
        colorFilter(g);
    }

    protected void colorFilter(PGraphics pg) {
        pg.hint(DISABLE_DEPTH_TEST);
        pg.noStroke();
        pg.fill(picker("filter", 0.1f,0).clr());
        pg.rect(0, 0, pg.width, pg.height);
        pg.hint(ENABLE_DEPTH_TEST);
    }

    private void registerExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveStateToFile));
    }

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
        saveReplay();
        savePGraphics(pg);
    }

    private void savePGraphics(PGraphics pg) {
        if (captureScreenshot) {
            captureScreenshot = false;
            screenshotsAlreadyCaptured++;
            String filename = captureDir + "screenshot_" + screenshotsAlreadyCaptured + ".jpg";
            println(filename + " saved");
            pg.save(filename);
        }
        int frameRecordingEnd = frameRecordingStarted + frameRecordingDuration + 1;
        if (!replayStack.isEmpty()) {
            frameRecordingEnd = min(frameRecordingEnd, frameCount + replayStack.size());
        }
        if (frameRecordingStarted > 0 && frameCount < frameRecordingEnd) {
            int frameNumber = frameCount - frameRecordingStarted + 1;
            println(frameNumber, "/", frameRecordingEnd - frameRecordingStarted - 1, "saved");
            pg.save(captureDir + frameNumber + ".jpg");
            loadReplay(frameNumber);
        }
    }

    private void saveReplay() {
        if (!replayStackRecordingPaused) {
            replayStack.add(getGuiState());
        }
    }

    private void loadReplay(int frameNumber) {
        if (replayStack.isEmpty()) {
            return;
        }
        if (frameNumber < replayStack.size()) {
            println("setting state for frame", frameNumber);
            setGuiState(replayStack.get(frameNumber));
        }
        if (frameNumber >= replayStack.size()) {
            replayStack.clear();
        }
    }

    int numberOfDigitsInFlooredNumber(float inputNumber) {
        return String.valueOf(floor(inputNumber)).length();
    }

    public String regenIdAndCaptureDir() {
        String newId = year() + nf(month(), 2) + nf(day(), 2) + "-" + nf(hour(), 2) + nf(minute(), 2) + nf(second(),
                2) + "_" + this.getClass().getSimpleName();
        captureDir = "out/capture/" + newId + "/";
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
        return easedAnimation(startFrame, duration, easingFactor, 0, 1);
    }

    private float easedAnimation(float startFrame, float duration, float easingFactor, float constrainMin,
                                 float constrainMax) {
        float animationNormalized = constrain(norm(frameCount, startFrame,
                startFrame + duration), constrainMin, constrainMax);
        return ease(animationNormalized, easingFactor);
    }

    protected float ease(float p, float g) {
        if (p < 0.5)
            return 0.5f * pow(2 * p, g);
        else
            return 1 - 0.5f * pow(2 * (1 - p), g);
    }

    // TRAY

    protected void spiralSphere(PGraphics pg) {
        group("planet");
        pg.beginShape(POINTS);
        pg.stroke(picker("stroke").clr());
        pg.strokeWeight(slider("weight", 5));
        pg.noFill();
        float N = slider("count", 3000);
        float s = 3.6f / sqrt(N);
        float dz = 2.0f / N;
        float lon = 0;
        float z = 1 - dz / 2;
        float scl = slider("scale", 260);
        for (int k = 0; k < N; k++) {
            float r = sqrt(1 - z * z);
            pg.vertex(cos(lon) * r * scl, sin(lon) * r * scl, z * scl);
            z = z - dz;
            lon = lon + s / r;
        }
        pg.endShape();
        pg.noStroke();
        if (!toggle("hollow")) {
            pg.fill(0);
            pg.sphereDetail(floor(slider("detail", 20)));
            pg.sphere(slider("scale") - slider("core", 5));
        }
    }

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
        float size = menuButtonSize;
        updateMenuButtonHide(x, y, hideButtonWidth, size);
        if (!trayVisible) {
            return;
        }
        x += hideButtonWidth;
        updateMenuButtonUndo(x, y, size, size);
        x += size;
        updateMenuButtonRedo(x, y, size, size);
        x += size;
        updateMenuButtonSave(x, y, size, size);
    }

    private void updateMenuButtonHide(float x, float y, float w, float h) {
        if (hideActivated(x, y, w, h)) {
            trayVisible = !trayVisible;
            trayWidth = trayVisible ? trayWidthWhenExtended : 0;
            keyboardSelectionIndex = 0;
            hideRotationStarted = frameCount;
        }
        float grayscale = (keyboardSelected(MENU_BUTTON_HIDE) || isMouseOver(x, y, w, h)) ? GRAYSCALE_TEXT_SELECTED :
                GRAYSCALE_TEXT_DARK;
        fill(grayscale);
        stroke(grayscale);
        float rotation = easedAnimation(hideRotationStarted, MENU_ROTATION_DURATION, MENU_ROTATION_EASING);
        if (trayVisible) {
            rotation += 1;
        }
        if (isMouseOver(x, y, w, h) || trayVisible) {
            displayMenuButtonHideShow(x, y, w, h, rotation * PI);
        }
    }

    private void updateMenuButtonUndo(float x, float y, float w, float h) {
        float rotation = easedAnimation(undoRotationStarted, MENU_ROTATION_DURATION, MENU_ROTATION_EASING);
        rotation -= constrain(norm(undoHoldDuration, 0, menuButtonHoldThreshold), 0, 1);
        displayStateButton(x, y, w, h, rotation * TWO_PI, false, MENU_BUTTON_UNDO, undoStack.size());
        boolean canUndo = undoStack.size() > 0;
        if (canUndo && trayVisible) {
            if (actions.contains(ACTION_UNDO) || isMousePressedHere(x, y, w, h)) {
                undoHoldDuration++;
            } else if (!isMouseOver(x, y, w, h)) {
                undoHoldDuration = 0;
            }
            if (mouseJustReleasedHere(x, y, w, h) || actionJustReleased(ACTION_UNDO)) {
                if (undoHoldDuration < menuButtonHoldThreshold) {
                    pushCurrentStateToRedo();
                    popUndoToCurrentState();
                } else {
                    while (!undoStack.isEmpty()) {
                        pushCurrentStateToRedo();
                        popUndoToCurrentState();
                    }
                }
                undoRotationStarted = frameCount;
                undoHoldDuration = 0;
            }
        }
    }

    private void updateMenuButtonRedo(float x, float y, float w, float h) {
        float rotation = easedAnimation(redoRotationStarted, MENU_ROTATION_DURATION, MENU_ROTATION_EASING);
        rotation -= constrain(norm(redoHoldDuration, 0, menuButtonHoldThreshold), 0, 1);
        displayStateButton(x, y, w, h, rotation * TWO_PI, true, MENU_BUTTON_REDO, redoStack.size());
        boolean canRedo = redoStack.size() > 0;
        if (canRedo && trayVisible) {
            if (actions.contains(ACTION_REDO) || isMousePressedHere(x, y, w, h)) {
                redoHoldDuration++;
            } else if (!isMouseOver(x, y, w, h)) {
                redoHoldDuration = 0;
            }
            if (mouseJustReleasedHere(x, y, w, h) || actionJustReleased(ACTION_REDO)) {
                if (redoHoldDuration < menuButtonHoldThreshold) {
                    pushCurrentStateToUndoWithoutClearingRedo();
                    popRedoToCurrentState();
                } else {
                    while (!redoStack.isEmpty()) {
                        pushCurrentStateToUndoWithoutClearingRedo();
                        popRedoToCurrentState();
                    }
                }
                redoRotationStarted = frameCount;
                redoHoldDuration = 0;
            }
        }
    }

    private void displayMenuButtonHideShow(float x, float y, float w, float h, float rotation) {
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

    private void updateMenuButtonSave(float x, float y, float w, float h) {
        if (activated(MENU_BUTTON_SAVE, x, y, w, h) || actions.contains(ACTION_SAVE)) {
            saveAnimationStarted = frameCount;
            saveStateToFile();
        }
        rectMode(CENTER);
        float animation = 1 - easedAnimation(saveAnimationStarted, MENU_ROTATION_DURATION, 3);
        if (animation == 0) {
            animation = 1;
        }
        displayMenuButtonSave(x, y, w, h, animation);
    }

    private void displayMenuButtonSave(float x, float y, float w, float h, float animation) {
        float grayscale = (keyboardSelected(MENU_BUTTON_SAVE) || isMouseOver(x, y, w, h)) ?
                GRAYSCALE_TEXT_SELECTED : GRAYSCALE_TEXT_DARK;
        stroke(grayscale);
        strokeWeight(2);
        noFill();
        rect(x + w * .5f, y + h * .5f, w * .5f * animation, h * .5f * animation);
        rect(x + w * .5f, y + h * .5f - animation * h * .12f, w * .25f * animation, h * .25f * animation);
    }

    private void displayStateButton(float x, float y, float w, float h, float rotation,
                                    boolean direction, String menuButtonType, int stackSize) {
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
        if (stackSize == 0) {
            float crossSize = .08f;
            line(-w * crossSize, -h * crossSize, w * crossSize, h * crossSize);
            line(-w * crossSize, h * crossSize, w * crossSize, -h * crossSize);
        }
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
        pushMatrix();
        translate(0, trayScrollOffset);
        if (actions.contains(ACTION_ACTIVATE) && !trayVisible && overlayVisible) {
            overlayVisible = false;
            while (actions.contains(ACTION_ACTIVATE)) {
                actions.remove(ACTION_ACTIVATE);
            }
        }
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
                    updateElement(group, el, y);
                    if (trayVisible) {
                        displayElement(group, el, x, y);
                    }
                }
                x -= cell * .5f;
            }
            y += cell;
        }
        popMatrix();
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
            } else if (!el.equals(overlayOwner)) {
                setOverlayOwner(el);
            } else if (el.equals(overlayOwner)) {
                overlayVisible = false;
            }
        }
    }

    private void displayElement(Group group, Element el, float x, float y) {
        boolean isSelected = keyboardSelected(group.name + el.name) ||
                isMouseOverScrollAware(0, y - cell, trayWidth, cell);
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
        trayWidthWhenExtended = max(minimumTrayWidth, findLongestNameWidth() + cell * 2);
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

    private boolean hideActivated(float x, float y, float w, float h) {
        return actions.contains(ACTION_HIDE) || mouseJustReleasedHere(x, y, w, h);
    }

    private boolean activated(String query, float x, float y, float w, float h) {
        return mouseJustReleasedHereScrollAware(x, y, w, h) || keyboardActivated(query);
    }

    private boolean mouseJustReleasedHereScrollAware(float x, float y, float w, float h) {
        return mouseJustReleasedHere(x, y + trayScrollOffset, w, h) && trayHasntMovedInAWhile();
    }

    private boolean mouseJustReleasedHere(float x, float y, float w, float h) {
        return mouseJustReleased() && isPointInRect(mouseX, mouseY, x, y, w, h);
    }

    private boolean keyboardActivated(String query) {
        return (overlayOwner != null && (overlayOwner.group + overlayOwner.name).equals(query) || keyboardSelected(query))
                && actions.contains(ACTION_ACTIVATE);
    }

    private boolean mouseJustReleased() {
        return pMousePressed && !mousePressed;
    }

    private boolean isMousePressedHere(float x, float y, float w, float h) {
        return mousePressed && isPointInRect(mouseX, mouseY, x, y, w, h);
    }

    private boolean mouseJustPressed() {
        return !pMousePressed && mousePressed;
    }

    protected boolean mouseJustPressedOutsideGui() {
        return !pMousePressed && mousePressed && isMouseOutsideGui();
    }

    private boolean isMouseOutsideGui() {
        return !trayVisible || !isPointInRect(mouseX, mouseY, 0, 0, trayWidth, height);
    }

    private boolean isMouseOverScrollAware(float x, float y, float w, float h) {
        return isMouseOver(x, y + trayScrollOffset, w, h);
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
        if (!keyboardActive || !trayVisible) {
            return false;
        }
        if ((query.equals(MENU_BUTTON_HIDE) && keyboardSelectionIndex == 0)
                || (query.equals(MENU_BUTTON_UNDO) && keyboardSelectionIndex == 1)
                || (query.equals(MENU_BUTTON_REDO) && keyboardSelectionIndex == 2)
                || (query.equals(MENU_BUTTON_SAVE) && keyboardSelectionIndex == 3)) {
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
//        println((key == CODED ? "code: " + keyCode : "key: " + key));
        keyboardActive = true;
        if (key == CODED) {
            if (keyboardKeysDoesntContain(keyCode, true)) {
                keyboardKeys.add(new Key(keyCode, true));
            }
        } else {
            if (keyboardKeysDoesntContain(key, false)) {
                keyboardKeys.add(new Key((int) key, false));
            }
        }
        if (key == 'j') {
            replayStackRecordingPaused = !replayStackRecordingPaused;
            println(replayStackRecordingPaused ? "replay recording paused" : "replay recording resumed");
        }
        if (key == 'k') {
            replayStackRecordingPaused = true;
            println("replay recording paused");
            frameRecordingStarted = frameCount + 1;
            id = regenIdAndCaptureDir();
        }
        if (key == 'i') {
            captureScreenshot = true;
            id = regenIdAndCaptureDir();
        }
    }

    private boolean keyboardKeysDoesntContain(int keyCode, boolean coded) {
        for (Key kk : keyboardKeys) {
            if (kk.character == keyCode && kk.coded == coded) {
                return false;
            }
        }
        return true;
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
                    if (!upAndDownArrowsControlOverlay() && kk.repeatCheck() && trayVisible) {
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
                    if (!upAndDownArrowsControlOverlay() && kk.repeatCheck() && trayVisible) {
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
                if (kk.character == 'z' || kk.character == 26) {
                    actions.add(ACTION_UNDO);
                }
                if (kk.character == 'y' || kk.character == 25) {
                    actions.add(ACTION_REDO);
                }
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
                    actions.add(ACTION_HIDE);
                }
                if (kk.character == 19) {
                    // CTRL + S
                    actions.add(ACTION_SAVE);
                }
                if (kk.character == 12) {
                    // CTRL + L
                    actions.add(ACTION_LOAD);
                }
            }
            kk.justPressed = false;
        }
        if (keyboardSelectionIndex >= keyboardSelectionLength()) {
            keyboardSelectionIndex = MENU_BUTTON_COUNT;
        }
        if (keyboardSelectionIndex < MENU_BUTTON_COUNT) {
            Group lastGroup = getLastGroup();
            if(lastGroup != null){
                if (!lastGroup.expanded) {
                    keyboardSelectionIndex = keyboardSelectionLength() - lastGroup.elements.size() - 1;
                } else {
                    keyboardSelectionIndex = keyboardSelectionLength() - 1;
                }
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

    private boolean actionJustReleased(String action) {
        return previousActions.contains(action) && !actions.contains(action);
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
        if (previousActions.contains(ACTION_ALT)) {
            if (isAnyElementKeyboardSelected()) {
                Element el = findKeyboardSelectedElement();
                group = el.group;
                if (forwardFacing) {
                    return group.elements.size() - group.elements.indexOf(el) - 1;
                } else {
                    return group.elements.indexOf(el);
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

    private int elementCount() {
        int sum = 0;
        for(Group group : groups){
            sum += group.elements.size();
        }
        return sum;
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
        if(groups.isEmpty()){
            return null;
        }
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

    private boolean elementDoesntExist(String elementName, String groupName) {
        return findElement(elementName, groupName) == null;
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

    private void pushCurrentStateToRedo() {
        redoStack.add(getGuiState());
    }

    private void pushStateToUndo(ArrayList<String> state) {
        setGuiState(state);
        pushCurrentStateToUndo();
    }

    private void pushCurrentStateToUndo() {
        redoStack.clear();
        undoStack.add(getGuiState());
    }

    private void pushStateToRedo(ArrayList<String> state) {
        redoStack.add(state);
    }

    private void pushCurrentStateToUndoWithoutClearingRedo() {
        undoStack.add(getGuiState());
    }

    private void popUndoToCurrentState() {
        if (undoStack.isEmpty()) {
            return;
        }
        setGuiState(undoStack.remove(undoStack.size() - 1));
    }

    private void popRedoToCurrentState() {
        if (redoStack.isEmpty()) {
            return;
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
            String[] splitState = state.split(SEPARATOR);
            if (state.startsWith(GROUP_PREFIX)) {
                Group group = findGroup(splitState[1]);
                if (group == null) {
                    continue;
                }
                group.setState(state);
            } else {
                Element el = findElement(splitState[1], splitState[0]);
                if (el == null) {
//                    println("element does not exist", splitState[0], splitState[1]);
                    continue;
                }
                el.setState(state);
            }
        }
    }

    void saveStateToFile() {
        pushCurrentStateToUndo();
        File file = dataFile(settingsPath());
        ArrayList<String> save = new ArrayList<String>(Arrays.asList(loadLastStateFromFile(false)));
        save.add(STATE_BEGIN);
        save.add(UNDO_PREFIX);
        save.addAll(undoStack.get(undoStack.size() - 1));
        save.add(STATE_END);
        String[] saveArray = arrayListToStringArray(save);
        saveStrings(file, saveArray);
    }

    private String[] arrayListToStringArray(ArrayList<String> input) {
        String[] array = new String[input.size()];
        for (int i = 0; i < input.size(); i++) {
            array[i] = input.get(i);
        }
        return array;
    }

    protected String[] loadLastStateFromFile(boolean alsoPush) {
        File file = dataFile(settingsPath());
        if (!file.exists()) {
            return new String[0];
        }
        String[] lines = loadStrings(file);
        if (alsoPush) {
            redoStack.clear();
            undoStack.clear();
            boolean pushingUndo = false;
            ArrayList<String> runningState = new ArrayList<String>();
            for (String line : lines) {
                if (line.startsWith(UNDO_PREFIX)) {
                    pushingUndo = true;
                } else if (line.startsWith(REDO_PREFIX)) {
                    pushingUndo = false;
                } else if (line.startsWith(STATE_BEGIN)) {
                    runningState.clear();
                } else if (line.startsWith(STATE_END)) {
                    if (pushingUndo) {
//                        println("pushing ", concat(runningState));
                        pushStateToUndo(runningState);
                        runningState.clear();
                    } else {
//                        println("pushing ", concat(runningState));
                        pushStateToRedo(runningState);
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

    private String settingsPath() {
        return "gui\\" + this.getClass().getSimpleName() + ".txt";
    }

    // SHADERS

    protected void radialBlurPass(PGraphics pg) {
        String radialBlur = "radialBlur.glsl";
        uniform(radialBlur).set("delta", slider("radial delta", 1));
        uniform(radialBlur).set("power", slider("radial power", 1));
        hotFilter(radialBlur, pg);

    }

    protected void splitPass(PGraphics pg) {
        String split = "split.glsl";
        uniform(split).set("delta", slider("split"));
        hotFilter(split, pg);

    }

    protected void chromaticAberrationPass(PGraphics pg) {
        String chromatic = "postFX\\chromaticAberrationFrag.glsl";
        uniform(chromatic).set("maxDistort", slider("chromatic", 5));
        hotFilter(chromatic, pg);

    }

    protected void noiseOffsetPass(PGraphics pg, float t) {
        String noiseOffset = "noiseOffset.glsl";
        uniform(noiseOffset).set("time", t);
        uniform(noiseOffset).set("mixAmt", slider("mix", 0, 1, .1f));
        uniform(noiseOffset).set("mag", slider("mag", 0, .01f, .001f));
        uniform(noiseOffset).set("frq", slider("frq", 0, 50, 8.5f));
        hotFilter(noiseOffset, pg);
    }

    protected void noisePass(float t, PGraphics pg) {
        String noise = "postFX/noiseFrag.glsl";
        uniform(noise).set("time", t);
        uniform(noise).set("amount", slider("noise magnitude", 0, .24f, .05f));
        uniform(noise).set("speed", slider("noise speed", 1));
        hotFilter(noise, pg);
    }

    protected void rgbSplitPassUniform(PGraphics pg) {
        String rgbSplit = "rgbSplitUniform.glsl";
        uniform(rgbSplit).set("delta", slider("RGB split", 2));
        hotFilter(rgbSplit, pg);
    }

    protected void rgbSplitPass(PGraphics pg) {
        String rgbSplit = "postFX/rgbSplitFrag.glsl";
        uniform(rgbSplit).set("delta", slider("RGB split", 10));
        hotFilter(rgbSplit, pg);
    }

    protected void saturationVibrancePass(PGraphics pg) {
        String saturationVibrance = "postFX/saturationVibranceFrag.glsl";
        uniform(saturationVibrance).set("saturation", slider("saturation", 0, 0.5f, 0));
        uniform(saturationVibrance).set("vibrance", slider("vibrance", 0, 0.5f, 0));
        hotFilter(saturationVibrance, pg);
    }

    protected void toonPass(PGraphics pg) {
        String toonPass = "postFX/toonFrag.glsl";
        hotFilter(toonPass, pg);
    }

    protected void brightnessContractFrag(PGraphics pg) {
        String brightnessContractPass = "postFX/brightnessContrastFrag.glsl";
        uniform(brightnessContractPass).set("brightness", slider("brightness", 1, false));
        uniform(brightnessContractPass).set("contrast", slider("contrast", 2));
        hotFilter(brightnessContractPass, pg);
    }

    protected void vignettePass(PGraphics pg) {
        String vignettePass = "postFX/vignetteFrag.glsl";
        uniform(vignettePass).set("amount", slider("vignette", 5));
        uniform(vignettePass).set("falloff", slider("falloff"));
        hotFilter(vignettePass, pg);
    }

    public PShader uniform(String fragPath) {
        ShaderSnapshot snapshot = findSnapshotByPath(fragPath);
        snapshot = initIfNull(snapshot, fragPath, null);
        return snapshot.compiledShader;
    }

    public PShader uniform(String fragPath, String vertPath) {
        ShaderSnapshot snapshot = findSnapshotByPath(fragPath);
        snapshot = initIfNull(snapshot, fragPath, vertPath);
        return snapshot.compiledShader;
    }

    public void hotFilter(String path, PGraphics canvas) {
        hotShader(path, null, true, canvas);
    }

    public void hotFilter(String path) {
        hotShader(path, null, true, g);
    }

    public void hotShader(String fragPath, String vertPath, PGraphics canvas) {
        hotShader(fragPath, vertPath, false, canvas);
    }

    public void hotShader(String fragPath, String vertPath) {
        hotShader(fragPath, vertPath, false, g);
    }

    public void hotShader(String fragPath, PGraphics canvas) {
        hotShader(fragPath, null, false, canvas);
    }

    public void hotShader(String fragPath) {
        hotShader(fragPath, null, false, g);
    }

    private void hotShader(String fragPath, String vertPath, boolean filter, PGraphics canvas) {
        ShaderSnapshot snapshot = findSnapshotByPath(fragPath);
        snapshot = initIfNull(snapshot, fragPath, vertPath);
        snapshot.update(filter, canvas);
    }

    private ShaderSnapshot initIfNull(ShaderSnapshot snapshot, String fragPath, String vertPath) {
        if (snapshot == null) {
            snapshot = new ShaderSnapshot(fragPath, vertPath);
            snapshots.add(snapshot);
        }
        return snapshot;
    }

    private ShaderSnapshot findSnapshotByPath(String path) {
        for (ShaderSnapshot snapshot : snapshots) {
            if (snapshot.fragPath.equals(path)) {
                return snapshot;
            }
        }
        return null;
    }

    protected float hueModulo(float hue) {
        while (hue < 0) {
            hue += 1;
        }
        hue %= 1;
        return hue;
    }

// CLASSES

    private class ShaderSnapshot {
        String fragPath;
        String vertPath;
        File fragFile;
        File vertFile;
        PShader compiledShader;
        long fragLastKnownModified, vertLastKnownModified, lastChecked;
        boolean compiledAtLeastOnce = false;
        long lastKnownUncompilable = -shaderRefreshRateInMillis;


        ShaderSnapshot(String fragPath, String vertPath) {
            if (vertPath != null) {
                compiledShader = loadShader(fragPath, vertPath);
                vertFile = dataFile(vertPath);
                vertLastKnownModified = vertFile.lastModified();
                if (!vertFile.isFile()) {
                    println("Could not find shader at " + vertFile.getPath());
                }
            } else {
                compiledShader = loadShader(fragPath);
            }
            fragFile = dataFile(fragPath);
            fragLastKnownModified = fragFile.lastModified();
            lastChecked = currentTimeMillis();
            if (!fragFile.isFile()) {
                println("Could not find shader at " + fragFile.getPath());
            }
            this.fragPath = fragPath;
            this.vertPath = vertPath;
        }

        @SuppressWarnings("ManualMinMaxCalculation")
        long max(long a, long b) {
            if (a > b) {
                return a;
            }
            return b;
        }

        void update(boolean filter, PGraphics pg) {
            long currentTimeMillis = currentTimeMillis();
            long lastModified = fragFile.lastModified();
            if (vertFile != null) {
                lastModified = max(lastModified, vertFile.lastModified());
            }
            if (compiledAtLeastOnce && currentTimeMillis < lastChecked + shaderRefreshRateInMillis) {
//                println("compiled at least once, not checking, standard apply");
                applyShader(compiledShader, filter, pg);
                return;
            }
            if (!compiledAtLeastOnce && lastModified > lastKnownUncompilable) {
//                println("first try");
                tryCompileNewVersion(filter, pg, lastModified);
                return;
            }
            lastChecked = currentTimeMillis;
            if (lastModified > fragLastKnownModified && lastModified > lastKnownUncompilable) {
//                println("file changed, repeat try");
                tryCompileNewVersion(filter, pg, lastModified);
            } else if (compiledAtLeastOnce) {
//                println("file didn't change, standard apply");
                applyShader(compiledShader, filter, pg);
            }
        }

        private void applyShader(PShader shader, boolean filter, PGraphics pg) {
            if (filter) {
                pg.filter(shader);
            } else {
                pg.shader(shader);
            }
        }

        private void tryCompileNewVersion(boolean filter, PGraphics pg, long lastModified) {
            try {
                PShader candidate;
                if (vertFile == null) {
                    candidate = loadShader(fragPath);
                } else {
                    candidate = loadShader(fragPath, vertPath);
                }
                // we need to call filter() or shader() here in order to catch any compilation errors and not halt
                // the sketch
                applyShader(candidate, filter, pg);
                compiledShader = candidate;
                compiledAtLeastOnce = true;
                fragLastKnownModified = lastModified;
            } catch (Exception ex) {
                lastKnownUncompilable = lastModified;
                println("\n" + fragFile.getName() + ": " + ex.getMessage());
            }
        }
    }

    private class Key {
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

    private class Group {
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
            fill((keyboardSelected(name) || isMouseOverScrollAware(0, y - cell, trayWidth, cell)) ?
                    GRAYSCALE_TEXT_SELECTED : GRAYSCALE_TEXT_DARK);
            textAlign(LEFT, BOTTOM);
            textSize(textSize);
            text(name, x, y);
        }

        public String getState() {
            return GROUP_PREFIX + SEPARATOR + name + SEPARATOR + expanded;
        }

        public void setState(String state) {
            String[] split = state.split(SEPARATOR);
            expanded = Boolean.parseBoolean(split[2]);
        }
    }

    private abstract class Element {
        public float lastSelected = -DESELECTION_FADEOUT_DURATION;
        Group group;
        String name;

        Element(Group group, String name) {
            this.group = group;
            this.name = name;
        }

        void reset() {

        }

        abstract boolean canHaveOverlay();

        String getState() {
            return group.name + SEPARATOR + name + SEPARATOR;
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
                float iNorm = norm(i, 0, detail - 1);
                if ((fadeIn && iNorm > animation) || (!fadeIn && iNorm < animation)) {
                    continue;
                }
                if (iNorm < .333f) {
                    float downwardStroke = norm(iNorm, 0, .333f);
                    float downwardX = lerp(checkMarkTopLeftX, lowestCheckMarkPointX, downwardStroke);
                    float downwardY = lerp(checkMarkTopLeftY, lowestCheckMarkPointY, downwardStroke);
                    vertex(downwardX, downwardY);
                    continue;
                }
                float upwardStroke = norm(iNorm, .333f, 1);
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

    private class Radio extends Element {
        ArrayList<String> options = new ArrayList<String>();
        int valueIndex = 0;

        Radio(Group parent, String name, String[] options) {
            super(parent, name);
            this.options.add(name);
            this.options.addAll(Arrays.asList(options));
        }

        String getState() {
            return super.getState() + valueIndex;
        }

        void setState(String newState) {
            valueIndex = Integer.parseInt(newState.split(SEPARATOR)[2]);
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
                float size = 4;
                float rectX = x + cell * .15f + i * size * 2.5f;
                if (i == valueIndex) {
                    size *= 1.8f;
                    pushStyle();
                    noFill();
                }
                rectMode(CENTER);
                rect(rectX, y + cell * .1f, size, size);
                if (i == valueIndex) {
                    popStyle();
                }
            }
        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {
            pushCurrentStateToUndo();
            valueIndex++;
            if (valueIndex >= options.size()) {
                valueIndex = 0;
            }
        }

        float trayTextWidth() {
            return textWidth(value());
        }
    }

    private class Button extends Element {
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

    private class Toggle extends Element {
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
            this.checked = Boolean.parseBoolean(newState.split(SEPARATOR)[2]);
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
                pushCurrentStateToUndo();
                reset();
            }
        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {
            pushCurrentStateToUndo();
            activationStarted = frameCount;
            checked = !checked;
        }
    }

    private abstract class Slider extends Element {
        Slider(Group parent, String name) {
            super(parent, name);
        }

        protected float updateFullHorizontalSlider(float x, float y, float w, float h, float value, float precision,
                                                   float horizontalRevealAnimationStarted, boolean alternative,
                                                   float minValue, float maxValue) {
            float deltaX = updateInfiniteSlider(precision, width, true, true, alternative);
            float horizontalAnimation = easedAnimation(horizontalRevealAnimationStarted - SLIDER_REVEAL_START_SKIP,
                    SLIDER_REVEAL_DURATION, SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(x + width * .5f, y, w, h,
                    precision, value, horizontalAnimation, true, true, false, minValue, maxValue);
            return deltaX;
        }

        @SuppressWarnings("SuspiciousNameCombination")
        protected float updateFullHeightVerticalSlider(float x, float y, float w, float h, float value, float precision,
                                                       float verticalRevealAnimationStarted, boolean alternative,
                                                       float minValue, float maxValue) {
            float deltaY = updateInfiniteSlider(precision, height, false, true, alternative);
            float verticalAnimation = easedAnimation(verticalRevealAnimationStarted - SLIDER_REVEAL_START_SKIP,
                    SLIDER_REVEAL_DURATION, SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(x + height * .5f, y, w, h,
                    precision, value, verticalAnimation, false, true, false, minValue, maxValue);
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
                return keyboardDelta(true, horizontal, precision);
            }
            if ((alternative && !previousActions.contains(ACTION_ALT)) ||
                    (!alternative && previousActions.contains(ACTION_ALT))) {
                return 0;
            }
            return keyboardDelta(false, horizontal, precision);
        }

        private float keyboardDelta(boolean directionless, boolean horizontal, float precision) {
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
                                             float revealAnimation, boolean horizontal, boolean cutout,
                                             boolean floored, float minValue, float maxValue) {
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
            displaySliderBackground(w, h, cutout, horizontal);
            float weight = 2;
            strokeWeight(weight);
            displayHorizontalLine(w, revealAnimation);
            if (!horizontal) {
                pushMatrix();
                scale(-1, 1);
            }
            displayMarkerLines(precision * 0.5f, 0, markerHeight * .6f, weight * revealAnimation,
                    true, value, precision, w, h, !horizontal, revealAnimation, minValue, maxValue);
            displayMarkerLines(precision * .05f, 10, markerHeight * .3f, weight * revealAnimation,
                    false, value, precision, w, h, !horizontal, revealAnimation, minValue, maxValue);
            if (!horizontal) {
                popMatrix();
            }
            displayValue(w, h, precision, value, revealAnimation, floored);
            popMatrix();
            popStyle();
        }

        void displaySliderBackground(float w, float h, boolean cutout, boolean horizontal) {
            fill(0, BACKGROUND_ALPHA);
            rectMode(CENTER);
            float xOffset = 0;
            if (cutout) {
                if (horizontal && trayVisible) {
                    xOffset = trayWidth;
                } else if (!horizontal) {
                    xOffset = h;
                }
            }
            rect(xOffset, 0, w, h);
        }

        void displayHorizontalLine(float w, float revealAnimation) {
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

        void displayMarkerLines(float frequency, int skipEveryNth, float markerHeight, float horizontalLineHeight,
                                boolean shouldDisplayValue, float value, float precision, float w, float h,
                                boolean flipTextHorizontally, float revealAnimationEased, float minValue,
                                float maxValue) {
            float markerValue = -precision - value - frequency;
            int i = 0;
            while (markerValue < precision - value) {
                markerValue += frequency;
                if (skipEveryNth != 0 && i++ % skipEveryNth == 0) {
                    continue;
                }
                float markerNorm = norm(markerValue, -precision - value, precision - value);
                displayMarkerLine(markerValue, precision, w, h, markerHeight, horizontalLineHeight, value,
                        shouldDisplayValue, flipTextHorizontally,
                        revealAnimationEased, minValue, maxValue);
            }
        }

        void displayMarkerLine(float markerValue, float precision, float w, float h, float markerHeight,
                               float horizontalLineHeight,
                               float value, boolean shouldDisplayValue, boolean flipTextHorizontally,
                               float revealAnimationEased, float minValue, float maxValue) {
            float moduloValue = markerValue;
            while (moduloValue > precision) {
                moduloValue -= precision * 2;
            }
            while (moduloValue < -precision) {
                moduloValue += precision * 2;
            }
            float screenX = map(moduloValue, -precision, precision, -w, w);
            float displayValue = moduloValue + value;
            boolean isEdgeValue =
                    (displayValue < minValue + precision * .1 && displayValue > minValue - precision * .1) ||
                    (displayValue > maxValue - precision * .1 && displayValue < maxValue + precision * .1);
            if (!isEdgeValue && (displayValue > maxValue || displayValue < minValue)) {
                return;
            }
            float grayscale = darkenEdges(screenX, w);
            fill(GRAYSCALE_TEXT_SELECTED, grayscale * revealAnimationEased);
            stroke(GRAYSCALE_TEXT_SELECTED, grayscale * revealAnimationEased);
            line(screenX, -markerHeight * .5f, screenX, -horizontalLineHeight * .5f);
            if (shouldDisplayValue) {
                if (flipTextHorizontally) {
                    pushMatrix();
                    scale(-1, 1);
                }
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

        void displayValue(float w, float sliderHeight, float precision, float value, float animationEased,
                          boolean floored) {
            fill(GRAYSCALE_TEXT_DARK);
            textAlign(CENTER, CENTER);
            textSize(textSize * 1.2f);
            float textY = -cell * 2.5f;
            float textX = 0;
            String text;
            if (floored) {
                text = String.valueOf(floor(value));
            } else if (abs(value) < 1) {
                if (abs(value) < precision * .001f) {
                    text = nf(value, 0, 0);
                } else {
                    text = String.valueOf(value);
                }
            } else {
                text = nf(value, 0, 2);
            }
            if (text.startsWith("-")) {
                textX -= textWidth("-") * .5f;
            }
            noStroke();
            fill(0, BACKGROUND_ALPHA);
            rectMode(CENTER);
            rect(textX, textY + textSize * .2f, textWidth(text) + 20, textSize * 1.2f + 20);
            fill(GRAYSCALE_TEXT_SELECTED * animationEased);
            text(text, textX, textY);
            stroke(GRAYSCALE_TEXT_SELECTED);
            line(0, -5, 0, 5);
        }

        float darkenEdges(float screenX, float w) {
            float xNorm = norm(screenX, -w, w);
            float distanceFromCenter = abs(.5f - xNorm) * 4;
            return 1 - ease(distanceFromCenter, SLIDER_EDGE_DARKEN_EASING);
        }

        void recordStateForUndo() {
            if (mouseJustPressedOutsideGui() || keyboardInteractionJustStarted()) {
                pushCurrentStateToUndo();
            }
        }

        boolean keyboardInteractionJustStarted() {
            boolean wasKeyboardActive = previousActions.contains(ACTION_LEFT) || previousActions.contains(ACTION_RIGHT);
            boolean isKeyboardActive = actions.contains(ACTION_LEFT) || actions.contains(ACTION_RIGHT);
            return !wasKeyboardActive && isKeyboardActive;
        }
    }

    private class SliderFloat extends Slider {
        boolean constrained, floored;
        float value, precision, defaultValue, defaultPrecision, minValue, maxValue, lastValueDelta;
        float sliderRevealAnimationStarted = -SLIDER_REVEAL_DURATION;

        SliderFloat(Group parent, String name, float defaultValue, float precision,
                    boolean constrained, float min, float max, boolean floored) {
            super(parent, name);
            this.value = defaultValue;
            this.defaultValue = defaultValue;
            this.precision = precision;
            this.defaultPrecision = precision;
            this.floored = floored;
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
            } else if (name.contains("count") || name.contains("size")) {
                this.constrained = true;
                minValue = 0;
                maxValue = Float.MAX_VALUE;
                if (value == 0) {
                    this.value = 1;
                }
            } else if (name.equals("drag")) {
                this.constrained = true;
                minValue = 0;
                maxValue = 1;
            }
        }

        void handleKeyboardInput() {
            if (previousActions.contains(ACTION_PRECISION_ZOOM_OUT) &&
                    ((!floored && precision < FLOAT_PRECISION_MAXIMUM) || (floored && precision < INT_PRECISION_MAXIMUM))) {
                precision *= 10f;
                pushCurrentStateToUndo();
            }
            if (previousActions.contains(ACTION_PRECISION_ZOOM_IN) &&
                    ((!floored && precision > FLOAT_PRECISION_MINIMUM) || (floored && precision > INT_PRECISION_MINIMUM))) {
                precision *= .1f;
                pushCurrentStateToUndo();
            }
            if (overlayOwner.equals(this) && actions.contains(ACTION_RESET)) {
                pushCurrentStateToUndo();
                reset();
            }
        }

        void reset() {
            precision = defaultPrecision;
            value = defaultValue;
        }

        String getState() {
            return super.getState() + value + SEPARATOR + precision;
        }

        void setState(String newState) {
            String[] split = newState.split(SEPARATOR);
//            println(name, "value", value);
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
            if (floored && valueDelta == 0 && lastValueDelta == 0) {
                value = lerp(value, round(value), .2f);
            }
            if (constrained) {
                value = constrain(value, minValue, maxValue);
            }
            float revealAnimation = easedAnimation(sliderRevealAnimationStarted - SLIDER_REVEAL_START_SKIP,
                    SLIDER_REVEAL_DURATION,
                    SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(width * .5f, height - cell, width, sliderHeight, precision,
                    value, revealAnimation, true, true, floored,
                    constrained ? minValue : -Float.MAX_VALUE,
                    constrained ? maxValue : Float.MAX_VALUE);
        }
    }

    private class SliderXY extends Slider {
        float deltaX;
        float deltaY;
        PVector value = new PVector();
        PVector defaultValue = new PVector();
        float precision, defaultPrecision;
        float horizontalRevealAnimationStarted = -SLIDER_REVEAL_DURATION;
        float verticalRevealAnimationStarted = -SLIDER_REVEAL_DURATION;
        float interactionBufferMultiplier = 2.5f;

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
            return super.getState() + precision + SEPARATOR + value.x + SEPARATOR + value.y;
        }

        void setState(String newState) {
            String[] xyz = newState.split(SEPARATOR);
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
            lockOtherSlidersOnMouseOver();
            value.x += deltaX;
            value.y += deltaY;
        }

        protected void lockOtherSlidersOnMouseOver() {
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
            return isMouseOver(0, height - cell * interactionBufferMultiplier, width, sliderHeight*2);
        }

        protected boolean isMouseOverYSlider() {
            return isMouseOver(width - cell * interactionBufferMultiplier, 0, sliderHeight*2, height);
        }


        void updateXYSliders() {
            deltaX = updateFullHorizontalSlider(0, height - cell, width, sliderHeight, value.x, precision,
                    horizontalRevealAnimationStarted, false, -Float.MAX_VALUE, Float.MAX_VALUE);
            deltaY = updateFullHeightVerticalSlider(0, width - cell, height, sliderHeight, value.y, precision,
                    verticalRevealAnimationStarted, false, -Float.MAX_VALUE, Float.MAX_VALUE);
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
            if (previousActions.contains(ACTION_PRECISION_ZOOM_IN) && precision > FLOAT_PRECISION_MINIMUM) {
                precision *= .1f;
                pushCurrentStateToUndo();
            }
            if (previousActions.contains(ACTION_PRECISION_ZOOM_OUT) && precision < FLOAT_PRECISION_MAXIMUM) {
                precision *= 10f;
                pushCurrentStateToUndo();
            }
            if (overlayOwner.equals(this) && actions.contains(ACTION_RESET)) {
                pushCurrentStateToUndo();
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

    private class SliderXYZ extends SliderXY {
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
            return super.getState() + SEPARATOR + value.z;
        }

        void setState(String newState) {
            super.setState(newState);
            value.z = Float.parseFloat(newState.split(SEPARATOR)[5]);
        }

        void updateOverlay() {
            recordStateForUndo();
            deltaZ = updateInfiniteSlider(precision, height * .5f, false, true, true);
            lockOtherSlidersOnMouseOver();
            value.x += deltaX;
            value.y += deltaY;
            value.z += deltaZ;
            float zAnimation = easedAnimation(zRevealAnimationStarted, SLIDER_REVEAL_DURATION, SLIDER_REVEAL_EASING);
            displayInfiniteSliderCenterMode(height - height * .2f, width - sliderHeight * 2, height / 3f,
                    sliderHeight * .8f, precision, value.z, zAnimation, false, false,
                    false, -Float.MAX_VALUE, Float.MAX_VALUE);
            super.updateXYSliders();
        }

        private boolean isMouseOverZSlider() {
            stroke(1);
            return isMouseOver(width - sliderHeight * 4, 0, sliderHeight * 3, height * .4f);
        }

        protected void lockOtherSlidersOnMouseOver() {
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

    public class HSBA {
        private float hue, sat, br, alpha;

        public HSBA(float hue, float sat, float br, float alpha) {
            this.hue = hue;
            this.sat = sat;
            this.br = br;
            this.alpha = alpha;
        }

        public HSBA() {
            this.alpha = 1;
        }

        public int clr() {
            pushStyle();
            enforceConstraints();
            colorMode(HSB, 1, 1, 1, 1);
            int result = color(hue, sat, br, alpha);
            popStyle();
            return result;
        }

        public float hue() {
            enforceConstraints();
            return hue;
        }

        public void addHue(float val) {
            hue += val;
            enforceConstraints();
        }

        public float sat() {
            enforceConstraints();
            return sat;
        }


        public void setSat(float val) {
            sat = val;
            enforceConstraints();
        }

        public float br() {
            enforceConstraints();
            return br;
        }

        public void setBr(float val) {
            br = val;
            enforceConstraints();
        }

        public float alpha() {
            enforceConstraints();
            return alpha;
        }

        private void enforceConstraints() {
            hue = hueModulo(hue);
            sat = constrain(sat, 0, 1);
            br = constrain(br, 0, 1);
            alpha = constrain(alpha, 0, 1);
        }

        public void setAlpha(float val) {
            alpha = val;
            enforceConstraints();
        }
    }

    private class ColorPicker extends Slider {
        HSBA hsba;
        float defaultHue, defaultSat, defaultBr, defaultAlpha;
        float pickerRevealStarted = -PICKER_REVEAL_DURATION;
        float huePrecision = .5f;
        float alphaPrecision = 1;
        private boolean brightnessLocked, saturationLocked;
        private boolean satChanged, brChanged;

        ColorPicker(Group currentGroup, String name, float hue, float sat, float br, float alpha) {
            super(currentGroup, name);
            this.hsba = new HSBA(hue, sat, br, alpha);
            this.defaultHue = hue;
            this.defaultSat = sat;
            this.defaultBr = br;
            this.defaultAlpha = alpha;
        }

        void handleKeyboardInput() {
            if (overlayOwner != null && overlayOwner.equals(this) && actions.contains(ACTION_RESET)) {
                pushCurrentStateToUndo();
                reset();
            }
            if (alphaPrecision > ALPHA_PRECISION_MINIMUM && previousActions.contains(ACTION_PRECISION_ZOOM_IN)) {
                alphaPrecision *= .1f;
                pushCurrentStateToUndo();
            }
            if (alphaPrecision < ALPHA_PRECISION_MAXIMUM && previousActions.contains(ACTION_PRECISION_ZOOM_OUT)) {
                alphaPrecision *= 10;
                pushCurrentStateToUndo();
            }
            if (previousActions.contains(ACTION_CONTROL)) {
                satChanged = true;
                if (actions.contains(ACTION_UP)) {
                    hsba.sat -= .01f;
                } else if (actions.contains(ACTION_DOWN)) {
                    hsba.sat += .01f;
                }
            }
            if (previousActions.contains(ACTION_ALT)) {
                brChanged = true;
                if (actions.contains(ACTION_UP)) {
                    hsba.br -= .01f;
                } else if (actions.contains(ACTION_DOWN)) {
                    hsba.br += .01f;
                }
            }
            hsba.enforceConstraints();
        }

        void reset() {
            hsba.hue = defaultHue;
            hsba.sat = defaultSat;
            hsba.br = defaultBr;
            hsba.alpha = defaultAlpha;
        }

        String getState() {
            return super.getState() + hsba.hue + SEPARATOR + hsba.sat + SEPARATOR + hsba.br + SEPARATOR + hsba.alpha + SEPARATOR + alphaPrecision;
        }

        void setState(String newState) {
            String[] split = newState.split(SEPARATOR);
            hsba.hue = Float.parseFloat(split[2]);
            hsba.sat = Float.parseFloat(split[3]);
            hsba.br = Float.parseFloat(split[4]);
            hsba.alpha = Float.parseFloat(split[5]);
            alphaPrecision = Float.parseFloat(split[6]);
        }

        void displayOnTray(float x, float y) {
            pushStyle();
            stroke(GRAYSCALE_TEXT_DARK);
            strokeWeight(1);
            fill(hsba.clr());
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

        @SuppressWarnings("SuspiciousNameCombination")
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
            float lastSat = hsba.sat;
            hsba.sat = updateTinySlider(x, tinySliderTopY, tinySliderWidth, h, brightnessLocked, SATURATION);
            if (hsba.sat != lastSat && !saturationLocked) {
                brightnessLocked = true;
            }
            if (saturationLocked) {
                hsba.sat = lastSat;
            }
            displayTinySlider(x, tinySliderTopY, tinySliderWidth, cell * 4, hsba.sat, SATURATION,
                    brightnessLocked);

            x += tinySliderWidth * 1.2f;
            float lastBr = hsba.br;
            hsba.br = updateTinySlider(x, tinySliderTopY, tinySliderWidth, h, saturationLocked, BRIGHTNESS);
            if (hsba.br != lastBr && !brightnessLocked) {
                saturationLocked = true;
            }
            if (brightnessLocked) {
                hsba.br = lastBr;
            }
            displayTinySlider(x, tinySliderTopY, tinySliderWidth, cell * 4, hsba.br, BRIGHTNESS, saturationLocked);

            displayInfiniteSliderCenterMode(height - height / 4f, width - sliderHeight * .5f, height / 2f, sliderHeight,
                    alphaPrecision, hsba.alpha, revealAnimation, false, false, false, 0, 1);
            fill(GRAYSCALE_TEXT_DARK);
            textAlign(CENTER, CENTER);
            textSize(textSize);
            text("alpha", width - sliderHeight * .5f, 15);
            float alphaDelta = updateInfiniteSlider(alphaPrecision, height, false, false, false);
            boolean isMouseInTopHalf = isMouseOver(width*.5f, 0, width*.5f, height / 2f);
            if (!satChanged && !brChanged && (keyboardActive || isMouseInTopHalf)) {
                hsba.alpha += alphaDelta;
            }

            displayHueSlider(sliderHeight, revealAnimation);
            float hueDelta = updateInfiniteSlider(huePrecision, width, true, false, false);
            if (!satChanged && !brChanged && (keyboardActive || !isMouseInTopHalf)) {
                hsba.hue += hueDelta;
            }
            satChanged = false;
            brChanged = false;
            hsba.enforceConstraints();
            displayValueRectangle(sliderHeight);
            popStyle();
        }

        private void displayHueSlider(float h, float revealAnimation) {
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
                float iHue = hueModulo(hsba.hue - .5f + iNorm);
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
            fill(hsba.clr());
            rectMode(CENTER);
            rect(x, y, cell * 3, cell * 3);
        }

        private float updateTinySlider(float x, float topY, float w, float h, boolean forceActive, String type) {
            float interactionBuffer = cell;
            if (forceActive || (mousePressed && isMouseOver(x, topY - interactionBuffer, w,
                    h + interactionBuffer * 1.2f))) {
                float newValue = constrain(map(mouseY, topY, topY + h, 0, 1), 0, 1);
                setTinySliderValue(newValue, type);
            }
            return getTinySliderValue(type);
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
                return color(value, hsba.sat, hsba.br, hsba.alpha);
            }
            if (type.equals(SATURATION)) {
                return color(hsba.hue, value, hsba.br, hsba.alpha);
            }
            if (type.equals(BRIGHTNESS)) {
                return color(hsba.hue, hsba.sat, value, hsba.alpha);
            }
            return 0;
        }


        private float getTinySliderValue(String type) {
            if (type.equals(SATURATION)) {
                return hsba.sat;
            }
            if (type.equals(BRIGHTNESS)) {
                return hsba.br;
            }
            return 0;
        }

        private void setTinySliderValue(float newValue, String type) {
            if (type.equals(SATURATION)) {
                hsba.sat = newValue;
                satChanged = true;
            }
            if (type.equals(BRIGHTNESS)) {
                hsba.br = newValue;
                brChanged = true;
            }
        }

        HSBA getHSBA() {
            return hsba;
        }
    }
}