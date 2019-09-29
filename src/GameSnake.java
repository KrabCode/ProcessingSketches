import applet.HotswapGuiSketch;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;

public class GameSnake extends HotswapGuiSketch {

    PGraphics pg;
    Animation squareFill;
    Animation squareStroke;
    Animation face;
    Animation circle;
    ControllerManager controllers;
    Snake player;
    ArrayList<Snake> snakes = new ArrayList<>();
    ArrayList<Fruit> fruits = new ArrayList<>();
    private float t;

    public static void main(String[] args) {
        HotswapGuiSketch.main("GameSnake");
    }

    public void settings() {
        fullScreen(P2D);
//        size(800, 800, P2D);
    }

    public void setup() {
//        surface.setAlwaysOnTop(true);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.colorMode(HSB, 1, 1, 1, 1);
        pg.endDraw();
        squareFill = new Animation("C:\\Projects\\ProcessingSketches\\data\\animation\\tvars_10fps\\ctverecFill");
        squareStroke = new Animation("C:\\Projects\\ProcessingSketches\\data\\animation\\tvars_10fps\\ctverec");
        face = new Animation("C:\\Projects\\ProcessingSketches\\data\\animation\\tvars_10fps\\face");
        circle = new Animation("C:\\Projects\\ProcessingSketches\\data\\animation\\tvars_10fps\\kruh");
        controllers = new ControllerManager();
        controllers.initSDLGamepad();
        player = new Snake();
        snakes.add(player);
    }

    public void draw() {
        t += radians(slider("t"));
        pg.beginDraw();
        alphaFade(pg);
        updateAnimations();
        updateSnakes();
        updateFruits();
        rgbSplitUniformPass(pg);
        noiseOffsetPass(t, pg);
        pg.endDraw();
        image(pg, 0, 0, width, height);
        rec(pg);
        gui(false);
    }

    private void updateFruits() {
        while (fruits.size() < slider("fruit count", 5)) {
            fruits.add(new Fruit());
        }
        for (Fruit f : fruits) {
            f.update();
        }
    }

    private void updateSnakes() {
        while (snakes.size() < controllers.getNumControllers()) {
            snakes.add(new Snake());
        }
        while (snakes.size() > controllers.getNumControllers()) {
            snakes.remove(snakes.size() - 1);
        }
        for (Snake s : snakes) {
            s.update();
        }
    }

    private void updateAnimations() {
        squareFill.update();
        squareStroke.update();
        face.update();
        circle.update();
    }

    class Animation {
        ArrayList<PImage> frames;
        int currentIndex;

        public Animation(String folder) {
            frames = loadImages(folder);
            for (PImage frame : frames) {
                frame.filter(INVERT);
            }
        }

        void update() {
            if (frameCount % floor(slider("speed", 1, 30)) == 0) {
                currentIndex++;
            }
            currentIndex %= frames.size();
        }

        public PImage get() {
            return frames.get(currentIndex);
        }

    }

    class Snake {
        int bodyLength = 100;
        ArrayList<PVector> body = new ArrayList<>();
        ArrayList<PVector> toRemove = new ArrayList<>();
        int segmentSize;
        float hue;

        Snake() {
            PVector head = snakeStartingPosition();
            body.add(head);
            segmentSize = squareFill.get().width;
        }

        int snakeIndex() {
            return snakes.indexOf(this);
        }

        void update() {
            PVector pos = body.get(body.size() - 1);
            float speed = slider("move speed", 20);
            ControllerState input = controllers.getState(controllerIndex());
            float x = 0;
            float y = 0;
            if (input.rightStickMagnitude > .18) {
                x += speed * input.rightStickX;
                y -= speed * input.rightStickY;
            }
            PVector newPos = new PVector(pos.x + x, pos.y + y);
            checkBounds(newPos);
            if (newPos.mag() > .1) {
                body.add(newPos);
            }
            while (body.size() > bodyLength && body.size() > 1) {
                body.remove(0);
            }
            for (PVector segment : body) {
                int segmentIndex = body.indexOf(segment);
                float segmentIndexNorm = norm(segmentIndex, 0, body.size());
                collideFruit();
                PVector collision = collideSnakes(segment);
                if (collision != null) {
                    pg.tint(0, 1, 1);
                    Snake collider = null;
                    for (Snake s : snakes) {
                        if (s.body.contains(collision)) {
                            collider = s;
                        }
                    }
                    collider.bodyLength -= 5;
                    bodyLength -= 5;
                    if (collider.bodyLength < 1) {
                        collider.bodyLength = 1;
                    }
                    if (bodyLength < 1) {
                        bodyLength = 1;
                    }
                    toRemove.add(collision);

                } else {
                    pg.tint((.2f + snakeIndex() * .5f + hue + .1f * sin(t - segmentIndexNorm * 3)) % 1, 1, 1.f);
                }
                pg.imageMode(CENTER);
                pg.image(squareFill.get(), segment.x, segment.y);
            }
            for (Snake s : snakes) {
                if (this.equals(s)) {
                    continue;
                }
                if (s.body.containsAll(toRemove)) {
                    PVector head = s.body.get(s.body.size() - 1);
                    s.body.removeAll(toRemove);
                    s.body.add(head);

                }
            }
            toRemove.clear();
            PVector head = body.get(body.size() - 1);
//            pg.textSize(36);
//            pg.textAlign(CENTER, CENTER);
//            pg.text(bodyLength, head.x, head.y - 10);

            pg.image(face.get(), head.x, head.y);
        }

        private void checkBounds(PVector newPos) {
            if (newPos.x < 0) {
                newPos.x += width;
            }
            if (newPos.x > width) {
                newPos.x -= width;
            }
            if (newPos.y < 0) {
                newPos.y += height;
            }
            if (newPos.y > height) {
                newPos.y -= height;
            }
        }

        private int controllerIndex() {
            return snakes.indexOf(this);
        }

        void collideFruit() {
            ArrayList<Fruit> toRemove = new ArrayList<>();
            for (Fruit f : fruits) {
                PVector head = body.get(body.size() - 1);
                float d = dist(head.x, head.y, f.pos.x, f.pos.y);
                if (d < segmentSize) {
                    toRemove.add(f);
                    bodyLength += 5;
                    f.pop();
                }
            }
            fruits.removeAll(toRemove);
            toRemove.clear();
        }

        PVector collideSnakes(PVector segment) {
            for (Snake s : snakes) {
                if (this.equals(s)) {
                    continue;
                }
                for (PVector bodyPart : s.body) {
                    float d = dist(segment.x, segment.y, bodyPart.x, bodyPart.y);
                    if (d < segmentSize) {
                        return bodyPart;
                    }
                }
            }
            return null;
        }

        private PVector snakeStartingPosition() {
            return new PVector(width / 2f + random(-200, 200), height / 2f + random(-200, 200));
        }
    }

    class Fruit {
        PVector pos = fruitSpawnPos();

        private PVector fruitSpawnPos() {
            return new PVector(random(width), random(height));
        }

        void update() {
            pg.tint(1, 1);
            pg.image(circle.get(), pos.x, pos.y);
        }

        public void pop() {
            pg.pushMatrix();
            pg.tint(0);
            pg.translate(pos.x, pos.y);
            pg.imageMode(CENTER);
//            pg.scale(1.5f);
            pg.image(circle.get(), 0, 0);
            pg.popMatrix();
        }
    }
}
