package applet.juicygui;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings({"InnerClassMayBeStatic", "SameParameterValue", "FieldCanBeLocal", "BooleanMethodIsAlwaysInverted", "unused", "ConstantConditions", "WeakerAccess"})
public abstract class JuicyGuiSketch extends PApplet {
    //TODO:
    // ----- Operation JUICE -----
    // fix the slider marker alpha problem
    // juicy special buttons, cool animations
    // hue picker slider
    // activation animation for extra juice
    // scrolling down to allow unlimited group and element count

    // state
    private static final String GROUP_MARKER = "GROUP";
    private static final String SEPARATOR = "_!_!!!_!_";
    private static final String ACTION_MIDDLE_MOUSE_BUTTON = "MIDDLE_MOUSE_BUTTON";
    private static final String ACTION_UP = "UP";
    private static final String ACTION_DOWN = "DOWN";
    private static final String ACTION_LEFT = "LEFT";
    private static final String ACTION_RIGHT = "RIGHT";
    private static final String ACTION_PRECISION_ZOOM_IN = "PRECISION_ZOOM_IN";
    private static final String ACTION_PRECISION_ZOOM_OUT = "PRECISION_ZOOM_OUT";
    private static final String ACTION_RESET = "RESET";
    private static final String ACTION_CONTROL = "CONTROL";
    private static final String ACTION_ACTIVATE = "ACTIVATE";
    private static final String ACTION_UNDO = "UNDO";
    private static final String ACTION_REDO = "REDO";

    // utils
    protected float t;
    private ArrayList<ArrayList<String>> undoStack = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> redoStack = new ArrayList<ArrayList<String>>();

    // color
    private float backgroundAlpha = .5f;
    private boolean onWindows;
    private ArrayList<Group> groups = new ArrayList<Group>();
    private Group currentGroup = null; // do not assign to nor read directly!
    private float grayscaleGrid = .3f;
    private float grayscaleTextSelected = 1;
    private float grayscaleText = .6f;

    // input
    private int menuButtonCount = 3;
    private static final String MENU_BUTTON_REDO = "redo";
    private static final String MENU_BUTTON_UNDO = "undo";
    private static final String MENU_BUTTON_HIDE = "hide";

    private ArrayList<Key> keyboardKeys = new ArrayList<Key>();
    private ArrayList<Key> keyboardKeysToRemove = new ArrayList<Key>();
    private ArrayList<String> actions = new ArrayList<String>();
    private ArrayList<String> previousActions = new ArrayList<String>();
    private int keyboardSelectionIndex = 0;
    private int keyRepeatDelayFirst = 300;
    private int keyRepeatDelay = 40;
    private boolean pMousePressed = false;

    // layout
    private float textSize = 24;
    private float cell = 40;
    private float minimumTrayWidth = cell * 6;
    private float trayWidth = minimumTrayWidth;
    private boolean trayVisible = true;
    private float animationEasingFactor = 3;

    // overlay
    private boolean overlayVisible;
    private boolean horizontalOverlayVisible;
    private boolean verticalOverlayVisible;
    private Element overlayOwner = null; // do not assign directly!
    private int overlayOwnershipTrayAnimationDuration = 10;
    private int overlayOwnershipTrayAnimationStarted = -overlayOwnershipTrayAnimationDuration;
    private float overlayDarkenEasingFactor = 3;
    private int overlayRevealAnimationDuration = 20;
    private float overlayRevealEasingFactor = 4;

    // INTERFACE

    protected float sliderFloat(String name, float defaultValue, float precision) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(name, currentGroup.name)) {
            SliderFloat newElement = new SliderFloat(currentGroup, name, defaultValue, precision);
            currentGroup.elements.add(newElement);
        }
        SliderFloat slider = (SliderFloat) findElement(name, currentGroup.name);
        assert slider != null;
        return slider.value;
    }

    protected PVector slider2D(String name, float defaultX, float defaultY, float precision) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(name, currentGroup.name)) {
            Slider2D newElement = new Slider2D(currentGroup, name, defaultX, defaultY, precision);
            currentGroup.elements.add(newElement);
        }
        Slider2D slider = (Slider2D) findElement(name, currentGroup.name);
        assert slider != null;
        return slider.value;
    }

    protected int sliderColor(String name, float hue, float sat, float br) {
        return sliderColor(name, hue, sat, br, 1.f);
    }

    protected int sliderColor(String name, float hue, float sat, float br, float alpha) {
        Group currentGroup = getCurrentGroup();
        if (!elementExists(name, currentGroup.name)) {
            SliderColor newElement = new SliderColor(currentGroup, name, hue, sat, br, alpha);
            currentGroup.elements.add(newElement);
        }
        SliderColor slider = (SliderColor) findElement(name, currentGroup.name);
        assert slider != null;
        return slider.value();
    }

    protected String radio(String defaultValue, String... otherValues) {
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
        pushStyle();
        pushMatrix();
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
        popMatrix();
        pMousePressed = mousePressed;
    }

    private void firstFrameSetup(boolean defaultVisibility) {
        trayVisible = defaultVisibility;
        onWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (!onWindows) {
            textSize *= 2;
            cell *= 2;
        }
        textSize(textSize * 2); //the maximum text size needs to be called first, otherwise the font is stretched and ugly
    }

    // UTILS

    private boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
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
        surface.setTitle(this.getClass().getSimpleName() + "\t" + fps);
    }

    private void updateSpecialButtons() {
        float x = 0;
        float y = 0;
        updateSpecialHideButton(x, y, cell * 2, cell);
        if (!trayVisible) {
            return;
        }
        x += cell * 2;
        updateSpecialUndoButton(x, y, cell * 2, cell);
        x += cell * 2;
        updateSpecialRedoButton(x, y, cell * 2, cell);
    }

    //TODO replace text with cute animated arrows
    private void updateSpecialHideButton(float x, float y, float w, float h) {
        if (activated(MENU_BUTTON_HIDE, x, y, w, h)) {
            trayVisible = !trayVisible;
            keyboardSelectionIndex = 0;
        }
        fill((keyboardSelected(MENU_BUTTON_HIDE) || isMouseOver(x, y, w, h)) ? grayscaleTextSelected : grayscaleText);
        if (isMouseOver(x, y, w, h) || trayVisible) {
            textSize(textSize);
            textAlign(CENTER, BOTTOM);
            text(trayVisible ? "hide" : "show", x, y, w, h);
        }
    }

    private void updateSpecialUndoButton(float x, float y, float w, float h) {
        boolean canUndo = undoStack.size() > 0;
        if (canUndo && (activated(MENU_BUTTON_UNDO, x, y, w, h) || actions.contains(ACTION_UNDO))) {
            pushCurrentStateToRedo();
            popUndoToCurrentState();
            keyboardSelectionIndex = 1;
        }
        drawMenuButton(MENU_BUTTON_UNDO, undoStack.size(), x, y, w, h);
    }

    private void updateSpecialRedoButton(float x, float y, float w, float h) {
        boolean canRedo = redoStack.size() > 0;
        if (canRedo && (activated(MENU_BUTTON_REDO, x, y, w, h) || actions.contains(ACTION_REDO))) {
            pushCurrentStateToUndoManually();
            popRedoToCurrentState();
            keyboardSelectionIndex = 2;
        }
        drawMenuButton(MENU_BUTTON_REDO, redoStack.size(), x, y, w, h);
    }

    private void drawMenuButton(String text, int number, float x, float y, float w, float h) {
        textSize(textSize);
        textAlign(CENTER, BOTTOM);
        fill((keyboardSelected(MENU_BUTTON_REDO) || isMouseOver(x, y, w, h)) ? grayscaleTextSelected : grayscaleText);
        text(text, x, y, w, h);
        textSize(textSize * .5f);
        textAlign(CENTER, CENTER);
        text(number, x + w * .9f, y + textSize * .4f);
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
                    el.handleKeyboardInput();
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
        if (!overlayVisible && activated(group.name, 0, y - cell, trayWidth, cell)) {
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
            } else if (overlayVisible && !el.equals(overlayOwner)) {
                setOverlayOwner(el);
            } else if (overlayVisible && el.equals(overlayOwner)) {
                overlayVisible = false;
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

    private void setOverlayOwner(Element overlayOwnerToSet) {
        this.overlayOwner = overlayOwnerToSet;
        this.overlayOwner.onOverlayShown();
        overlayVisible = true;
        overlayOwnershipTrayAnimationStarted = frameCount;
    }

    // INPUT

    private boolean activated(String query, float x, float y, float w, float h) {
        return mouseJustReleasedHere(x, y, w, h) || keyboardActivated(query);
    }

    private boolean keyboardActivated(String query) {
        return keyboardSelected(query) && actions.contains(ACTION_ACTIVATE);
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
        return !isPointInRect(mouseX, mouseY, 0, 0, trayWidth, height);
    }

    private boolean isMouseOver(float x, float y, float w, float h) {
        return onWindows && (frameCount > 1) && isPointInRect(mouseX, mouseY, x, y, w, h);
    }

    public void mouseReleased() {
        if (mouseButton == CENTER) {
            actions.add(ACTION_MIDDLE_MOUSE_BUTTON);
        }
    }

    public void mouseWheel(MouseEvent event) {
        float direction = event.getCount();
        if (direction > 0) {
            actions.add(ACTION_PRECISION_ZOOM_IN);
        } else if (direction < 0) {
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
        if (!trayVisible) {
            keyboardSelectionIndex = 0;
        }
        if ((query.equals(MENU_BUTTON_HIDE) && keyboardSelectionIndex == 0)
                || (query.equals(MENU_BUTTON_UNDO) && keyboardSelectionIndex == 1)
                || (query.equals(MENU_BUTTON_REDO) && keyboardSelectionIndex == 2)) {
            return true;
        }
        int i = menuButtonCount;
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
        return menuButtonCount + groups.size() + elementCount;
    }

    public void keyPressed() {
        if (key == CODED) {
            if (!isKeyPressed(keyCode, true)) {
                keyboardKeys.add(new Key(keyCode, true));
            }
        } else {
            if (!isKeyPressed(key, false)) {
                keyboardKeys.add(new Key((int) key, false));
            }
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
                        if (keyboardSelectionIndex == menuButtonCount) {
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
                        if (keyboardSelectionIndex < menuButtonCount) {
                            keyboardSelectionIndex = menuButtonCount;
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
            } else if (!kk.coded) {
                if (!kk.justPressed) {
                    continue;
                }
                if (kk.character == '*' || kk.character == '+') {
                    actions.add(ACTION_PRECISION_ZOOM_OUT);
                }
                if (kk.character == '/' || kk.character == '-') {
                    actions.add(ACTION_PRECISION_ZOOM_IN);
                }
                if (kk.character == ' ' || kk.character == ENTER) {
                    actions.add(ACTION_ACTIVATE);
                }
                if (kk.character == 'r') {
                    actions.add(ACTION_RESET);
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
        return overlayVisible && verticalOverlayVisible;
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

    // GROUPS AND ELEMENTS

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
            Group anonymous = new Group("");
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

    private void popUndoToCurrentState() {
        if (undoStack.size() == 0) {
            throw new IllegalStateException("nothing to pop in undo stack!");
        }
        overwriteState(undoStack.remove(undoStack.size() - 1));
    }

    private void popRedoToCurrentState() {
        if (redoStack.size() == 0) {
            throw new IllegalStateException("nothing to pop in redo stack!");
        }
        overwriteState(redoStack.remove(redoStack.size() - 1));
    }

    private void overwriteState(ArrayList<String> statesToSet) {
        for (String elementState : statesToSet) {
            if (elementState.startsWith(GROUP_MARKER)) {
                Group group = findGroup(elementState.split(SEPARATOR)[1]);
                group.overwriteState(elementState);
            } else {
                String[] splitState = elementState.split(SEPARATOR);
                Element el = findElement(splitState[1], splitState[0]);
                el.overwriteState(elementState);
            }
        }
    }

    private ArrayList<String> getGuiState() {
        ArrayList<String> state = new ArrayList<String>();
        for (Group group : groups) {
            state.add(group.getState());
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

    // GROUPS and control ELEMENTS

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
                    (!repeatedAlready && millis() > lastRegistered + keyRepeatDelayFirst) ||
                    (repeatedAlready && millis() > lastRegistered + keyRepeatDelay);
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

        String getState() {
            return GROUP_MARKER + SEPARATOR + name + SEPARATOR + expanded;
        }

        void overwriteState(String newState) {
            this.expanded = Boolean.parseBoolean(newState.split(SEPARATOR)[2]);
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
            return parent.name + SEPARATOR + name + SEPARATOR;
        }

        abstract void overwriteState(String newState);

        abstract boolean canHaveOverlay();

        void update() {

        }

        void updateOverlay() {

        }

        void onActivationWithoutOverlay(int x, float y, float w, float h) {

        }

        void displayOnTray(float x, float y) {
            textAlign(LEFT, BOTTOM);
            textSize(textSize);
            if (overlayVisible && this.equals(overlayOwner)) {
                underlineAnimation(overlayOwnershipTrayAnimationStarted, overlayOwnershipTrayAnimationDuration, x, y, true);
            }
            text(name, x, y);
        }

        float trayTextWidth() {
            return textWidth(name);
        }


        void underlineAnimation(int startFrame, int duration, float x, float y, boolean stayExtended) {
            float fullWidth = textWidth(name);
            float animation = constrain(norm(frameCount, startFrame, startFrame + duration), 0, 1);
            float animationEased = ease(animation, animationEasingFactor);
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

        void onOverlayShown() {

        }

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
                state.append(SEPARATOR);
                state.append(option);
            }
            return state.toString();
        }

        void overwriteState(String newState) {
            valueIndex = Integer.parseInt(newState.split(SEPARATOR)[2]);
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

        void overwriteState(String newState) {

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

        void overwriteState(String newState) {
            this.state = Boolean.parseBoolean(newState.split(SEPARATOR)[2]);
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

        void update() {
            if (actions.contains(ACTION_RESET)) {
                state = defaultState;
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

        float updateInfiniteSlider(float precision, float sliderWidth, boolean horizontallyControlled, boolean mouseWheelHorizontal) {
            if (mousePressed && isMouseOutsideGui()) {
                float screenSpaceDelta = horizontallyControlled ? (pmouseX - mouseX) : (pmouseY - mouseY);
                float valueSpaceDelta = screenDistanceToValueDistance(screenSpaceDelta, precision, sliderWidth);
                if (valueSpaceDelta != 0) {
                    return valueSpaceDelta;
                }
            }
            if (actions.contains(ACTION_LEFT) && horizontallyControlled) {
                return screenDistanceToValueDistance(-3, precision, sliderWidth);
            }
            if (actions.contains(ACTION_RIGHT) && horizontallyControlled) {
                return screenDistanceToValueDistance(3, precision, sliderWidth);
            }
            if (actions.contains(ACTION_UP) && !horizontallyControlled) {
                return screenDistanceToValueDistance(-3, precision, sliderWidth);
            }
            if (actions.contains(ACTION_DOWN) && !horizontallyControlled) {
                return screenDistanceToValueDistance(3, precision, sliderWidth);
            }
            return 0;
        }

        float screenDistanceToValueDistance(float screenSpaceDelta, float precision, float sliderWidth) {
            float valueToScreenRatio = precision / sliderWidth;
            return screenSpaceDelta * valueToScreenRatio;
        }

        void displayInfiniteSliderCenterMode(float x, float y, float w, float h, float precision, float value, boolean horizontal, float revealAnimation) {
            float markerHeight = h * revealAnimation;
            pushMatrix();
            pushStyle();
            translate(x, y);
            noStroke();
            drawSliderBackground(w, h, !horizontal);
            float horizontalLineWeight = 3;
            drawHorizontalLine(w, horizontalLineWeight, revealAnimation);
            if (!horizontal) {
                pushMatrix();
                scale(-1, 1);
            }
            drawMarkerLines(precision * 0.5f, 0, markerHeight * .5f,
                    horizontalLineWeight, true, value, precision, w, h, !horizontal, revealAnimation);
            drawMarkerLines(precision * .05f, 10, markerHeight * .3f,
                    horizontalLineWeight, false, value, precision, w, h, !horizontal, revealAnimation);
            if (!horizontal) {
                popMatrix();
            }
            drawValue(h, precision, value, revealAnimation);
            popMatrix();
            popStyle();
        }

        void drawSliderBackground(float w, float h, boolean verticalCutout) {
            fill(0, backgroundAlpha);
            rectMode(CENTER);
            rect(0, 0, verticalCutout ? w - h * 2 : w, h);
        }

        void drawHorizontalLine(float w, float weight, float animation) {
            stroke(grayscaleText);
            beginShape();
            strokeWeight(weight * animation);
            for (int i = 0; i < w; i++) {
                float iNorm = norm(i, 0, w);
                float screenX = lerp(-w, w, iNorm);
                stroke(grayscaleTextSelected, distanceFromCenterGrayscale(screenX, w));
                vertex(screenX, 0);
            }
            endShape();
        }

        void drawMarkerLines(float frequency, int skipEveryNth, float markerHeight, float horizontalLineHeight,
                             boolean shouldDrawValue, float value, float precision, float w, float h,
                             boolean flipTextHorizontally, float revealAnimationEased) {
            float markerValue = -precision - value;
            int i = 0;
            while (markerValue <= precision - value) {
                if (skipEveryNth != 0 && i++ % skipEveryNth == 0) {
                    markerValue += frequency;
                    continue;
                }
                float markerNorm = norm(markerValue, -precision - value, precision - value);


                drawMarkerLine(markerValue, precision, w, h, markerHeight, horizontalLineHeight, value, shouldDrawValue, flipTextHorizontally, revealAnimationEased);
                markerValue += frequency;
            }
        }

        private void drawMarkerLine(float markerValue, float precision, float w, float h, float markerHeight, float horizontalLineHeight,
                                    float value, boolean shouldDrawValue, boolean flipTextHorizontally, float revealAnimationEased) {
            float moduloValue = markerValue;
            while (moduloValue > precision) {
                moduloValue -= precision * 2;
            }
            while (moduloValue < -precision) {
                moduloValue += precision * 2;
            }
            float screenX = map(moduloValue, -precision, precision, -w, w);
            float grayscale = distanceFromCenterGrayscale(screenX, w);
            fill(grayscaleTextSelected, grayscale * revealAnimationEased);
            stroke(grayscaleTextSelected, grayscale * revealAnimationEased);
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
            fill(grayscaleText);
            textAlign(CENTER, CENTER);
            textSize(textSize * 2);
            float textY = -cell * 2;
            float textX = 0;
            String text = nf(value, 0, 2);
            if (text.startsWith("-")) {
                textX -= textWidth("-") * .5f;
            }
            noStroke();
            fill(0, backgroundAlpha);
            rectMode(CENTER);
            rect(textX, textY + textSize * .33f, textWidth(text) + 20, textSize * 2 + 20);
            fill(grayscaleTextSelected * animationEased);
            text(text, textX, textY);
        }

        float distanceFromCenterGrayscale(float screenX, float w) {
            float xNorm = norm(screenX, -w, w);
            float distanceFromCenter = abs(.5f - xNorm) * 4;
            return 1 - ease(distanceFromCenter, overlayDarkenEasingFactor);
        }
    }

    class SliderFloat extends Slider {
        float value, precision, defaultValue, defaultPrecision, minValue, maxValue, lastValueDelta;
        boolean constrained = false;
        private int overlayRevealAnimationStarted = -overlayRevealAnimationDuration;

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

        void handleKeyboardInput() {
            if (actions.contains(ACTION_PRECISION_ZOOM_OUT) && precision > .1f) {
                precision *= .1f;
            }
            if (actions.contains(ACTION_PRECISION_ZOOM_IN) && precision < 100000) {
                precision *= 10f;
            }
            if (actions.contains(ACTION_RESET)) {
                pushCurrentStateToUndoNaturally();
                precision = defaultPrecision;
                value = defaultValue;
            }
        }

        String getState() {
            return super.getState() + value;
        }

        void overwriteState(String newState) {
            String[] state = newState.split(SEPARATOR);
            value = Float.parseFloat(state[2]);
        }

        void onOverlayShown() {
            if (!overlayVisible || !horizontalOverlayVisible) {
                overlayRevealAnimationStarted = frameCount;
            }
            horizontalOverlayVisible = true;
            verticalOverlayVisible = false;
        }

        boolean canHaveOverlay() {
            return true;
        }

        void updateOverlay() {
            float valueDelta = updateInfiniteSlider(precision, width, true, true);
            recordInteractionForUndo(valueDelta);
            value += valueDelta;
            lastValueDelta = valueDelta;
            if (constrained) {
                value = constrain(value, minValue, maxValue);
            }
            float revealAnimation = easedAnimation(overlayRevealAnimationStarted, overlayRevealAnimationDuration, overlayRevealEasingFactor);
            displayInfiniteSliderCenterMode(width * .5f, height - cell, width, cell * 2, precision, value, true, revealAnimation);
        }

        void recordInteractionForUndo(float valueDelta) {
            if (mouseJustPressedOutsideGui() || keyboardInteractionJustStarted()) {
                pushCurrentStateToUndoNaturally();
            }
        }

        private boolean keyboardInteractionJustStarted() {
            boolean wasKeyboardActive = previousActions.contains(ACTION_LEFT) || previousActions.contains(ACTION_RIGHT);
            boolean isKeyboardActive = actions.contains(ACTION_LEFT) || actions.contains(ACTION_RIGHT);
            return !wasKeyboardActive && isKeyboardActive;
        }
    }

    class Slider2D extends Slider {
        PVector value = new PVector();
        PVector defaultValue = new PVector();
        PVector lastValueDelta = new PVector();
        float precision, defaultPrecision;
        private boolean mouseWheelHorizontal = false;
        private int horizontalOverlayRevealAnimationStarted = -overlayRevealAnimationDuration;
        private int verticalOverlayRevealAnimationStarted = -overlayRevealAnimationDuration;


        Slider2D(Group currentGroup, String name, float defaultX, float defaultY, float precision) {
            super(currentGroup, name);
            this.precision = precision;
            this.defaultPrecision = precision;
            value.x = defaultX;
            value.y = defaultY;
            defaultValue.x = defaultX;
            defaultValue.y = defaultY;
        }

        String getState() {
            return super.getState() + value.x + SEPARATOR + value.y;
        }

        void overwriteState(String newState) {
            String[] xyz = newState.split(SEPARATOR);
            value.x = Float.parseFloat(xyz[2]);
            value.y = Float.parseFloat(xyz[3]);
        }

        boolean canHaveOverlay() {
            return true;
        }

        void onOverlayShown() {
            if (!overlayVisible || !horizontalOverlayVisible) {
                horizontalOverlayRevealAnimationStarted = frameCount;
            }
            if (!overlayVisible || !verticalOverlayVisible) {
                verticalOverlayRevealAnimationStarted = frameCount;
            }
            horizontalOverlayVisible = true;
            verticalOverlayVisible = true;
        }

        void updateOverlay() {
            PVector valueDelta = new PVector();
            valueDelta.x = updateInfiniteSlider(precision, width, true, mouseWheelHorizontal);
            float horizontalAnimation = easedAnimation(horizontalOverlayRevealAnimationStarted, overlayRevealAnimationDuration, overlayRevealEasingFactor);
            displayInfiniteSliderCenterMode(width * .5f, height - cell, width, cell * 2, precision, value.x, true, horizontalAnimation);
            translate(width * .5f, height * .5f);
            rotate(-HALF_PI);
            translate(-height * .5f, -width * .5f);
            valueDelta.y = updateInfiniteSlider(precision, width, false, mouseWheelHorizontal);
            float verticalAnimation = easedAnimation(verticalOverlayRevealAnimationStarted, overlayRevealAnimationDuration, overlayRevealEasingFactor);
            displayInfiniteSliderCenterMode(height * .5f, width - cell, height, cell * 2, precision, value.y, false, verticalAnimation);
            recordInteractionForUndo(valueDelta);
            value.x += valueDelta.x;
            value.y += valueDelta.y;
            lastValueDelta.x = valueDelta.x;
            lastValueDelta.y = valueDelta.y;
        }

        void recordInteractionForUndo(PVector valueDelta) {
            if (mouseJustPressedOutsideGui() || keyboardInteractionJustStarted()) {
                pushCurrentStateToUndoNaturally();
            }
        }

        private boolean keyboardInteractionJustStarted() {
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
            if (actions.contains(ACTION_PRECISION_ZOOM_OUT) && precision > .1f) {
                precision *= .1f;
            }
            if (actions.contains(ACTION_PRECISION_ZOOM_IN) && precision < 10000) {
                precision *= 10f;
            }
            if (actions.contains(ACTION_RESET)) {
                pushCurrentStateToUndoNaturally();
                precision = defaultPrecision;
                value.x = defaultValue.x;
                value.y = defaultValue.y;
            }
            if (actions.contains(ACTION_MIDDLE_MOUSE_BUTTON)) {
                mouseWheelHorizontal = !mouseWheelHorizontal;
            }
        }
    }

    class SliderColor extends Slider {
        float hue, sat, br, alpha, defaultHue, defaultSat, defaultBr, defaultAlpha;

        SliderColor(Group currentGroup, String name, float hue, float sat, float br, float alpha) {
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
            if (actions.contains(ACTION_RESET)) {
                hue = defaultHue;
                sat = defaultSat;
                br = defaultBr;
                alpha = defaultAlpha;
            }
        }

        String getState() {
            return super.getState() + hue + SEPARATOR + sat + SEPARATOR + br + SEPARATOR + alpha;
        }

        void overwriteState(String newState) {
            String[] hsba = newState.split(SEPARATOR);
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
            return false;
        }

        void updateOverlay() {
            //TODO hue and alpha sliders, brightness/saturation grid
        }

    }

}
