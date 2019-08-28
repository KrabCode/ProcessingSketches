import applet.GuiSketch;
import peasy.PeasyCam;
import processing.opengl.PShader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static java.lang.System.currentTimeMillis;

public class LiveShaderTest extends GuiSketch {
    public static void main(String[] args) {
        GuiSketch.main("ShaderTest");
    }

    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        new PeasyCam(this, 600);

    }

    public void draw() {
        background(0);
        noFill();
        strokeWeight(10);
        stroke(255);
        box(200);
        liveShader("rgbSplit.glsl").set("strength", slider("strength", .005f));
        liveShader("rgbSplit.glsl").set("easing", slider("easing", 2));
        filter(liveShader("rgbSplit.glsl"));
        gui();
    }

    ArrayList<ShaderSnapshot> snapshots = new ArrayList<ShaderSnapshot>();
    int refreshRateInMillis = 1000;

    private PShader liveShader(String path) {
        ShaderSnapshot shader = findShaderByPath(path);
        if (shader == null) {
            shader = new ShaderSnapshot(path);
            snapshots.add(shader);
            println("new shader");
        }
        shader.update();
        return shader.compiledShader;
    }

    private ShaderSnapshot findShaderByPath(String path) {
        for (ShaderSnapshot snapshot : snapshots) {
            if (snapshot.path.equals(path)) {
                return snapshot;
            }
        }
        return null;
    }

    class ShaderSnapshot {
        String path;
        PShader compiledShader;
        File shaderFile;
        long lastKnownModified = -refreshRateInMillis;
        long lastChecked = -refreshRateInMillis;

        ShaderSnapshot(String filename) {
            compiledShader = loadShader(filename);
            shaderFile = new File(dataPath(filename));
            if (!shaderFile.exists()) {
                shaderFile = sketchFile(filename);
            }

            if (!shaderFile.exists()) {
                //TODO make file exist
                println("file does not exist");
            }
            this.path = filename;
        }

        void update() {
            long currentTimeMillis = currentTimeMillis();
            if (currentTimeMillis < lastChecked + refreshRateInMillis) {
                return;
            }
            lastChecked = currentTimeMillis;
            long lastModified = shaderFile.lastModified();
            println("checked " + lastModified + " > " + lastKnownModified);
            if (lastModified > lastKnownModified) {
                try {
                    compiledShader = loadShader(path);
                    lastKnownModified = lastModified;
                    println("reloaded");
                } catch (Exception ex) {
                    println(ex.getMessage());
                }
            }
        }
    }
}
