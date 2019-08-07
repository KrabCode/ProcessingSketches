package applet;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

@SuppressWarnings("DuplicatedCode")
public class Sketch extends PApplet {
    private String sketchName = this.getClass().getSimpleName();
    private String id = sketchName + "_" + year() + nf(month(), 2) + nf(day(), 2) + "-" + nf(hour(), 2) + nf(minute(), 2) + nf(second(), 2);
    protected String captureDir = "out/capture/" + id + "/";

    public void setup() {
        if (width < 1000) {
            surface.setLocation(1920 - width - 20, 20);
        }
    }

    public void draw() {
        float nonFlickeringFrameRate = frameRate > 58 && frameRate < 62 ? 60 : frameRate;
        surface.setTitle(sketchName + " (" + floor(nonFlickeringFrameRate) + " fps)");
    }

    private float rowWidthWindowFraction = 1 / 3f;
    private float rowHeightWindowFraction = 1 / 14f;
    private float elementPaddingFractionX = .9f;
    private float elementPaddingFractionY = .8f;
    private float slidersPerRow = 1;
    private float textActive = 0;
    private float textPassive = .7f;
    private float pressedFill = .7f;
    private float mouseOutsideStroke = .5f;
    private float mouseOverStroke = 1f;
    private ArrayList<GuiElement> allElements = new ArrayList<GuiElement>();
    private ArrayList<GuiElement> activeElements = new ArrayList<GuiElement>();

    private float cogR;
    private float vertexCount = 50;
    private ArrayList<PVector> cogShape;
    private ArrayList<PVector> arrowShape;
    private boolean extensionTogglePressedLastFrame = false;
    private int extensionToggleFadeoutDuration = 60;
    private int extensionToggleFadeoutDelay = 0;
    private int lastInteractedWithExtensionToggle = -extensionToggleFadeoutDelay - extensionToggleFadeoutDuration;
    private float extensionAnimationDuration = 60;
    private float extensionAnimationStarted = -extensionAnimationDuration;
    private float extensionEasing = -1;
    private float extensionAnimationTarget = -1;
    private float offsetXextendedWindowFraction = (1 / 24f);
    private float offsetXretractedWindowFraction = -rowWidthWindowFraction;
    private float offsetYWindowFraction = (1 / 24f);
    private float backgroundAlpha = .8f;

    protected void resetGui() {
        allElements.clear();
    }

    protected void gui() {
        gui(true);
    }

    protected void gui(boolean extendedByDefault) {
        if (isGuiEmpty()) {
            return;
        }
        pushMatrix();
        pushStyle();
        hint(DISABLE_DEPTH_TEST);
        resetMatrixInAnyRenderer();
        colorMode(HSB, 1, 1, 1, 1);
        drawBackground();
        updateExtension(extendedByDefault);
        drawExtensionToggle();
        for (GuiElement ge : allElements) {
            if (ge.lastQueried == frameCount) {
                updateDrawElement(ge);
            }
        }
        activeElements.clear();
        hint(ENABLE_DEPTH_TEST);
        popStyle();
        popMatrix();
    }

    private boolean isGuiEmpty() {
        return activeElements.isEmpty();
    }

    private void resetMatrixInAnyRenderer() {
        if (sketchRenderer().equals(P3D)) {
            camera();
        } else {
            resetMatrix();
        }
    }

    private void drawBackground() {
        noStroke();
        fill(0, backgroundAlpha);
        PVector offset = getOffset();
        float w = width * rowWidthWindowFraction + offset.x;
        float h = height * rowHeightWindowFraction * activeElements.size() + offset.y + cogR;
        rectMode(CORNER);
        rect(0, 0, w, h);
    }

    protected boolean button(String name) {
        Button button = (Button) findElement(name);
        if (button == null) {
            button = new Button(name, false, false);
        }
        button.lastQueried = frameCount;
        if (!allElements.contains(button)) {
            allElements.add(button);
        }
        if (!activeElements.contains(button)) {
            activeElements.add(button);
        }
        return button.value;
    }

    protected boolean toggle(String name) {
        return toggle(name, false);
    }

    protected boolean toggle(String name, boolean initial) {
        Button toggle = (Button) findElement(name);
        if (toggle == null) {
            toggle = new Button(name, true, initial);
        }
        toggle.lastQueried = frameCount;
        if (!allElements.contains(toggle)) {
            allElements.add(toggle);
        }
        if (!activeElements.contains(toggle)) {
            activeElements.add(toggle);
        }
        return toggle.value;
    }

    protected float slider(String name) {
        return slider(name, 0, 1);
    }

    protected float slider(String name, float max) {
        return slider(name, 0, max);
    }

    protected float slider(String name, float min, float max) {
        float range = max - min;
        return slider(name, min, max, min + range / 2);
    }

    protected float slider(String name, float min, float max, float initial) {
        Slider slider = (Slider) findElement(name);
        if (slider == null) {
            slider = new Slider(name, min, max, initial);
        }
        slider.lastQueried = frameCount;
        if (!allElements.contains(slider)) {
            allElements.add(slider);
        }
        if (!activeElements.contains(slider)) {
            activeElements.add(slider);
        }
        return slider.value;
    }

    private void updateDrawElement(GuiElement ge) {
        if (ge.getClass().getSimpleName().equals("Button")) {
            Button b = (Button) ge;
            if (b.isToggle) {
                updateDrawToggle(b);
            } else {
                updateDrawButton(b);
            }
        } else {
            updateDrawSlider((Slider) ge);
        }
    }

    private void updateDrawButton(Button button) {
        PVector pos = getPosition(button);
        float w = (width * rowWidthWindowFraction) * elementPaddingFractionX;
        float h = height * rowHeightWindowFraction * elementPaddingFractionY;
        boolean wasPressedLastFrame = button.pressed;
        boolean mouseOver = isPointInRect(mouseX, mouseY, pos.x, pos.y, w, h);
        button.pressed = mousePressed && mouseOver;
        button.value = wasPressedLastFrame && !button.pressed && !mousePressed;
        noFill();
        if (button.pressed) {
            fill(pressedFill);
        }
        stroke(button.pressed ? mouseOverStroke : mouseOutsideStroke);
        strokeWeight(1);
        rectMode(CORNER);
        rect(pos.x, pos.y, w, h);
        fill(button.pressed ? textActive : textPassive);
        textSize(h * .5f);
        textAlign(CENTER, CENTER);
        text(button.name, pos.x, pos.y, w, h);
    }

    private void updateDrawToggle(Button toggle) {
        PVector pos = getPosition(toggle);
        float w = (width * rowWidthWindowFraction) * elementPaddingFractionX;
        float h = height * rowHeightWindowFraction * elementPaddingFractionY;
        boolean wasPressedLastFrame = toggle.pressed;
        boolean mouseOver = isPointInRect(mouseX, mouseY, pos.x, pos.y, w, h);
        toggle.pressed = mousePressed && mouseOver;
        if (wasPressedLastFrame && !toggle.pressed && !mousePressed) {
            toggle.value = !toggle.value;
        }
        noFill();
        if (toggle.value) {
            fill(pressedFill);
        }
        stroke(toggle.pressed ? mouseOverStroke : mouseOutsideStroke);
        strokeWeight(1);
        rectMode(CORNER);
        rect(pos.x, pos.y, w, h);
        fill(toggle.value ? textActive : textPassive);
        if (toggle.pressed) {
            fill(mouseOverStroke);
        }
        textSize(h * .5f);
        textAlign(CENTER, CENTER);
        text(toggle.name, pos.x, pos.y, w, h);
    }

    private void updateDrawSlider(Slider slider) {
        PVector pos = getPosition(slider);
        float w = ((width * rowWidthWindowFraction) / slidersPerRow) * elementPaddingFractionX;
        float h = height * rowHeightWindowFraction * elementPaddingFractionY;
        float extraSensitivity = 20;
        float gray = mouseOutsideStroke;
        float alpha = 1;

        if (isPointInRect(mouseX, mouseY, pos.x - extraSensitivity, pos.y, w + extraSensitivity * 2, h)) {
            gray = mouseOverStroke;
            stroke(gray, alpha);
            if (mousePressed) {
                slider.value = map(mouseX, pos.x, pos.x + w, slider.min, slider.max);
                slider.value = constrain(slider.value, slider.min, slider.max);
            }
        }

        strokeCap(PROJECT);
        strokeWeight(1);
        stroke(gray, alpha);
        rectMode(CORNER);
        float sliderY = pos.y + h * .5f;
        line(pos.x, sliderY, pos.x + w, sliderY);
        float valueX = map(slider.value, slider.min, slider.max, pos.x, pos.x + w);

        strokeWeight(2);
        stroke(gray, alpha);
        line(valueX, pos.y, valueX, pos.y + h * .6f);

        fill(gray, alpha);
        textAlign(LEFT, CENTER);
        float textOffsetX = w * .05f;
        float textOffsetY = h * .75f;
        float defaultTextSize = h * .5f;
        float textWidth = w * 2;
        float textSize = defaultTextSize;
        while (textWidth > w * .5f) {
            textSize(textSize -= .5);
            textWidth = textWidth(slider.name);
        }
        text(slider.name, pos.x + textOffsetX, pos.y + textOffsetY);
        textAlign(RIGHT, CENTER);

        int floorBoundary = 10;
        String humanReadableValue;
        if (abs(slider.value) < floorBoundary) {
            humanReadableValue = nf(slider.value, 0, 0);
        } else {
            humanReadableValue = String.valueOf(round(slider.value));
        }
        text(humanReadableValue, pos.x + w - textOffsetX, pos.y + textOffsetY);
    }

    private PVector getPosition(GuiElement element) {
        float rowHeight = height * rowHeightWindowFraction;
        float rowWidth = width * rowWidthWindowFraction;
        PVector offset = getOffset();
        int index = activeElements.indexOf(element);
        int row = floor(index);
        int column = 0;
        return new PVector(offset.x + column * (rowWidth), offset.y + row * rowHeight);
    }

    private void updateExtension(boolean extendedByDefault) {
        float previousBaseR = cogR;
        cogR = min(width, height) * rowHeightWindowFraction * .4f;
        if (cogShape == null || previousBaseR != cogR) {
            cogShape = createCog();
            arrowShape = createArrow();
        }
        float extensionLinearNormalized = constrain(map(frameCount, extensionAnimationStarted, extensionAnimationStarted + extensionAnimationDuration, 0, 1), 0, 1);
        extensionEasing = ease(extensionLinearNormalized, 3);
        if (extensionAnimationTarget == -1) {
            if (extendedByDefault) {
                extensionAnimationTarget = 1;
            } else {
                extensionAnimationTarget = 0;
            }
        }
        if (extensionAnimationTarget == 0) {
            extensionEasing = 1 - extensionEasing;
        }
    }

    private ArrayList<PVector> createArrow() {
        ArrayList<PVector> arrow = new ArrayList<PVector>();
        for (int i = 0; i < vertexCount; i++) {
            float iN = map(i, 0, vertexCount - 1, 0, 2);
            if (iN < 1) {
                float x = cogR * abs(.5f - iN) * 2 - cogR * .5f - cogR * .15f;
                float y = lerp(-cogR, cogR, iN);
                arrow.add(new PVector(x, y));
            } else {
                iN = 2 - iN;
                float x = cogR * abs(.5f - iN) * 2 - cogR * .5f + cogR * .15f;
                float y = lerp(-cogR, cogR, iN);
                arrow.add(new PVector(x, y));
            }
        }
        return arrow;
    }

    private ArrayList<PVector> createCog() {
        ArrayList<PVector> cog = new ArrayList<PVector>();
        float toothR = cogR * .2f;
        for (int i = 0; i < vertexCount; i++) {
            float iNormalized = map(i, 0, vertexCount - 1, 0, 1);
            float a = iNormalized * TWO_PI;
            float r = cogR + toothR * (sin(iNormalized * 50) > 0 ? 0 : 1);
            cog.add(new PVector(r * cos(a), r * sin(a)));
        }
        return cog;
    }

    private void drawExtensionToggle() {
        float x = width * offsetXextendedWindowFraction + cogR;
        float y = cogR * 1.5f;
        if (extensionEasing > 0) {
            lastInteractedWithExtensionToggle = frameCount;
        }
        float alpha = 1 - constrain(map(frameCount,
                lastInteractedWithExtensionToggle + extensionToggleFadeoutDelay,
                lastInteractedWithExtensionToggle + extensionToggleFadeoutDelay + extensionToggleFadeoutDuration,
                0, 1), 0, 1);
        stroke(mouseOutsideStroke, alpha);
        noFill();
        strokeWeight(2);
        boolean atEitherEnd = extensionEasing == 0 || extensionEasing == 1;
        boolean justReleasedMouse = extensionTogglePressedLastFrame && !mousePressed;
        if (isPointInRect(mouseX, mouseY, x - cogR * 3, y - cogR, cogR * 6, cogR * 2)) {
            lastInteractedWithExtensionToggle = frameCount;
            if (atEitherEnd && justReleasedMouse) {
                startExtensionAnimation();
            }
            if (mousePressed) {
                extensionTogglePressedLastFrame = true;
            }
        }
        if (!mousePressed) {
            extensionTogglePressedLastFrame = false;
        }
        beginShape();
        for (int i = 0; i < vertexCount; i++) {
            PVector cogVertex = cogShape.get(i);
            PVector arrowVertex = arrowShape.get(i);
            PVector shapeVector = new PVector(lerp(cogVertex.x, arrowVertex.x, extensionEasing), lerp(cogVertex.y, arrowVertex.y, extensionEasing));
            vertex(x + shapeVector.x, y + shapeVector.y);
        }
        endShape(CLOSE);
        fill(mouseOutsideStroke, alpha);
        textSize(20);
        textAlign(LEFT,CENTER);
        text(sketchName, x+cogR*1.5f, y);
    }

    private void startExtensionAnimation() {
        boolean extend = extensionEasing == 0;
        extensionAnimationStarted = frameCount;
        if (extend) {
            extensionAnimationTarget = 1;
        } else {
            extensionAnimationTarget = 0;
        }
    }

    private PVector getOffset() {
        float offsetXWindowFraction = map(extensionEasing, 0, 1, offsetXretractedWindowFraction, offsetXextendedWindowFraction);
        float offsetX = width * offsetXWindowFraction;
        float offsetY = height * offsetYWindowFraction + cogR * 1.5f;
        return new PVector(offsetX, offsetY);
    }

    protected float ease(float p, float g) {
        if (p < 0.5)
            return 0.5f * pow(2 * p, g);
        else
            return 1 - 0.5f * pow(2 * (1 - p), g);
    }

    private boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }

    private GuiElement findElement(String query) {
        for (GuiElement ge : allElements) {
            if (ge.name.equals(query)) {
                return ge;
            }
        }
        return null;
    }

    private class GuiElement {
        String name;
        int lastQueried = 0;

        GuiElement(String name) {
            this.name = name;
        }
    }

    private class Slider extends GuiElement {
        float min, max, initial, value;

        Slider(String name, float min, float max, float initial) {
            super(name);
            this.min = min;
            this.max = max;
            this.initial = initial;
            this.value = initial;
            allElements.add(this);
        }
    }

    private class Button extends GuiElement {
        boolean pressed, value, initial, isToggle;

        Button(String name, boolean isToggle, boolean initial) {
            super(name);
            this.isToggle = isToggle;
            if (isToggle) {
                this.initial = initial;
                this.value = initial;
            }
            allElements.add(this);
        }
    }
}
