package applet;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

@SuppressWarnings("DuplicatedCode")
public class GuiPApplet extends PApplet {
    private String name = this.getClass().getSimpleName();
    private String id = name + "_" + year() + nf(month(), 2) + nf(day(), 2) + "-" + nf(hour(), 2) + nf(minute(), 2) + nf(second(), 2);
    protected String captureDir = "out/capture/" + id + "/";

    private ArrayList<Slider> sliders = new ArrayList<Slider>();
    private ArrayList<Button> buttons = new ArrayList<Button>();
    private ArrayList<Toggle> toggles = new ArrayList<Toggle>();
    float buttonsPerRow = 2;
    float togglesPerRow = 2;
    float slidersPerRow = 1;
    private float rowWidthWindowFraction = 1 / 3f;
    private float rowHeightWindowFraction = 1 / 12f;
    private float elementPaddingFractionX = .9f;
    private float elementPaddingFractionY = .8f;

    float textPassive = .3f;
    float textActive = 0;


    float pressedFill = .3f;
    private float mouseOutsideStroke = .3f;
    private float mouseOverStroke = .5f;

    public void setup() {
        if (width < 1000) {
            surface.setLocation(1920 - width - 20, 20);
        }
    }

    public void draw() {
        float nonFlickeringFrameRate = frameRate > 58 && frameRate < 62 ? 60 : frameRate;
        surface.setTitle(name + " (" + floor(nonFlickeringFrameRate) + " fps)");
    }

    protected boolean button(String name) {
        Button button = findButtonByName(name);
        if (button == null) {
            button = new Button(name);
        }
        if (hasGuiElementBeenQueriedThisFrame(button)) {
            return button.value;
        }
        button.lastFrameQueried = frameCount;
        updateElement(button);
        return button.value;
    }

    protected boolean toggle(String name) {
        return toggle(name, false);
    }

    protected boolean toggle(String name, boolean initial) {
        Toggle toggle = findToggleByName(name);
        if (toggle == null) {
            toggle = new Toggle(name, initial);
        }
        if (hasGuiElementBeenQueriedThisFrame(toggle)) {
            return toggle.value;
        }
        toggle.lastFrameQueried = frameCount;
        updateElement(toggle);
        return toggle.value;
    }

    protected float slider(String name) {
        return slider(name, 0, 1);
    }

    protected float slider(String name, float min, float max) {
        float range = max - min;
        return slider(name, min, max, min + range / 2);
    }

    protected float slider(String name, float min, float max, float initial) {
        Slider slider = findSliderByName(name);
        if (slider == null) {
            slider = new Slider(name, min, max, initial);
        }
        if (hasGuiElementBeenQueriedThisFrame(slider)) {
            return slider.value;
        }
        slider.lastFrameQueried = frameCount;
        updateElement(slider);
        return slider.value;
    }

    private void updateElement(GuiElement element) {
        pushMatrix();
        pushStyle();
        resetMatrixInAnyRenderer();
        colorMode(HSB, 1, 1, 1, 1);
        PVector pos = getPosition(element);
        switch (element.getClass().getSimpleName()) {
            case "Slider":
                updateSlider((Slider) element, pos);
                break;
            case "Button":
                updateButton((Button) element, pos);
                break;
            case "Toggle":
                updateToggle((Toggle) element, pos);
                break;
            default:
                break;
        }
        popStyle();
        popMatrix();
    }

    private void resetMatrixInAnyRenderer() {
        if (sketchRenderer().equals(P3D)) {
            camera();
        } else {
            resetMatrix();
        }
    }

    private void updateButton(Button button, PVector pos) {
        float w = ((width * rowWidthWindowFraction) / buttonsPerRow) * elementPaddingFractionX;
        float h = height * rowHeightWindowFraction * elementPaddingFractionY;
        boolean wasPressedLastFrame = button.pressed;
        boolean mouseOver = isPointInRect(mouseX, mouseY, pos.x, pos.y, w, h);
        button.pressed = mousePressed && mouseOver;
        button.value = wasPressedLastFrame && !button.pressed && !mousePressed;
        noFill();
        if(button.pressed){
            fill(pressedFill);
        }
        stroke(mouseOver? mouseOverStroke : mouseOutsideStroke);
        strokeWeight(1);
        rectMode(CORNER);
        rect(pos.x, pos.y, w, h);
        fill(button.pressed ? textActive : textPassive);
        textAlign(CENTER, CENTER);
        text(button.name, pos.x, pos.y, w, h);
    }

    private void updateToggle(Toggle toggle, PVector pos) {
        float w = ((width * rowWidthWindowFraction) / togglesPerRow) * elementPaddingFractionX;
        float h = height * rowHeightWindowFraction * elementPaddingFractionY;
        boolean wasPressedLastFrame = toggle.pressed;
        boolean mouseOver = isPointInRect(mouseX, mouseY, pos.x, pos.y, w, h);
        toggle.pressed = mousePressed && mouseOver;
        if (wasPressedLastFrame && !toggle.pressed && !mousePressed) {
            toggle.value = !toggle.value;
        }
        noFill();
        if(toggle.value){
            fill(pressedFill);
        }
        stroke(mouseOver? mouseOverStroke : mouseOutsideStroke);
        strokeWeight(1);
        rectMode(CORNER);
        rect(pos.x, pos.y, w, h);
        fill(toggle.value ? textActive : textPassive);
        textAlign(CENTER, CENTER);
        text(toggle.name, pos.x, pos.y, w, h);
    }

    private void updateSlider(Slider slider, PVector pos) {
        float w = width * rowWidthWindowFraction;
        float h = height * rowHeightWindowFraction * elementPaddingFractionX;
        float extraSensitivity = 20;
        float gray = mouseOutsideStroke;
        float alpha = 1;

        // update values
        if (isPointInRect(mouseX, mouseY, pos.x - extraSensitivity, pos.y, w + extraSensitivity * 2, h)) {
            gray = mouseOverStroke;
            stroke(gray, alpha);
            if(mousePressed){
                slider.value = map(mouseX, pos.x, pos.x + w, slider.min, slider.max);
                slider.value = constrain(slider.value, slider.min, slider.max);
            }
        }

        // draw slider
        strokeCap(PROJECT);
        strokeWeight(1);
        stroke(gray, alpha);
        rectMode(CORNER);
        float sliderY = pos.y + h * .5f;
        line(pos.x, sliderY, pos.x + w, sliderY);
        float valueX = map(slider.value, slider.min, slider.max, pos.x, pos.x + w);

        // draw selection bar
        strokeWeight(3);
        stroke(gray, alpha);
        line(valueX, pos.y, valueX, pos.y + h * .6f);

        // draw text
        fill(gray, alpha);
        textAlign(LEFT, CENTER);
        int textOffset = 5;
        text(slider.name, pos.x + textOffset, pos.y + h * .25f);
        textAlign(RIGHT, CENTER);

        // disregard values after floating point if value > floorBoundary
        int floorBoundary = 10;
        String humanReadableValue;
        if (abs(slider.value) < floorBoundary) {
            humanReadableValue = nf(slider.value, 0, 0);
        } else {
            humanReadableValue = String.valueOf(round(slider.value));
        }
        text(humanReadableValue, pos.x + w - textOffset, pos.y + h * .25f);
    }

    private PVector getPosition(GuiElement element) {
        float rowHeight = height * rowHeightWindowFraction;
        float rowWidth = width * rowWidthWindowFraction;
        int xOffset = width / 24;
        int yOffset = height / 24;
        int buttonRows = ceil(buttons.size() / buttonsPerRow);
        int toggleRows = ceil(toggles.size() / togglesPerRow);
        int row, column, itemsPerRow;

        String className = element.getClass().getSimpleName();
        switch (className) {
            case "Button": {
                int index = buttons.indexOf(element);
                itemsPerRow = floor(buttonsPerRow);
                row = floor(index / buttonsPerRow);
                column = floor(index % buttonsPerRow);
                break;
            }
            case "Toggle": {
                int index = toggles.indexOf(element);
                itemsPerRow = floor(togglesPerRow);
                row = buttonRows + floor(index / togglesPerRow);
                column = floor(index % togglesPerRow);
                break;
            }
            case "Slider": {
                int index = sliders.indexOf(element);
                itemsPerRow = floor(slidersPerRow);
                row = buttonRows + toggleRows + floor(index / slidersPerRow);
                column = floor(index % slidersPerRow);
                break;
            }
            default:
                row = 0;
                column = 0;
                itemsPerRow = 0;
                break;
        }

        return new PVector(xOffset + column * (rowWidth / itemsPerRow), yOffset + row * rowHeight);
    }

    private boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }

    private Slider findSliderByName(String query) {
        for (Slider s : sliders) {
            if (s.name.equals(query)) {
                return s;
            }
        }
        return null;
    }

    private Button findButtonByName(String query) {
        for (Button b : buttons) {
            if (b.name.equals(query)) {
                return b;
            }
        }
        return null;
    }

    private Toggle findToggleByName(String query) {
        for (Toggle t : toggles) {
            if (t.name.equals(query)) {
                return t;
            }
        }
        return null;
    }

    private boolean hasGuiElementBeenQueriedThisFrame(GuiElement element) {
        return element.lastFrameQueried == frameCount;
    }

    class GuiElement {
        String name;
        int lastFrameQueried;

        GuiElement(String name) {
            this.name = name;
        }
    }

    class Slider extends GuiElement {
        float min, max, initial, value;

        Slider(String name, float min, float max, float initial) {
            super(name);
            this.min = min;
            this.max = max;
            this.initial = initial;
            this.value = initial;
            sliders.add(this);
        }
    }

    class Button extends GuiElement {
        boolean pressed, value;

        Button(String name) {
            super(name);
            buttons.add(this);
        }
    }

    class Toggle extends GuiElement {
        boolean value, initial, pressed;

        Toggle(String name, boolean initial) {
            super(name);
            toggles.add(this);
            this.initial = initial;
            this.value = initial;
        }
    }
}
