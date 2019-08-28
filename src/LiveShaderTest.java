import applet.GuiSketch;
import processing.opengl.PShader;

import java.io.File;
import java.util.ArrayList;

import static java.lang.System.currentTimeMillis;

public class LiveShaderTest extends GuiSketch {
    ArrayList<ShaderSnapshot> snapshots = new ArrayList<ShaderSnapshot>();
    int refreshRateInMillis = 100;

    public static void main(String[] args) {
        GuiSketch.main("LiveShaderTest");
    }

    public void settings() {
        size(800, 800, P2D);
    }

/* the following code can hotswap the shader as you edit it as long as you change the lastModified timestamp of the file in your editor (try CTRL+S)
//
// USAGE: call hotFilter or hotShader once in the draw function to
//    - a) apply the current filter/shader and
//    - b) obtain a current reference to the shader and set your uniforms to it
*/

    public void setup() {

    }

    public void draw() {
        //TODO why can't I set the uniform?
        hotFilter("frag.glsl").set("time", radians(frameCount));
        hotFilter("frag.glsl");
    }

    public PShader hotFilter(String path) {
        return hotShader(path, true);
    }

    public PShader hotShader(String path) {
        return hotShader(path, false);
    }

    private PShader hotShader(String path, boolean filter) {
        ShaderSnapshot snapshot = findSnapshotByPath(path);
        if (snapshot == null) {
            snapshot = new ShaderSnapshot(path);
            snapshots.add(snapshot);
        }
        snapshot.update(filter);
        return snapshot.compiledShader;
    }

    private ShaderSnapshot findSnapshotByPath(String path) {
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
        long lastKnownUncompilable = -refreshRateInMillis;
        long lastChecked = -refreshRateInMillis;

        ShaderSnapshot(String filename) {
            compiledShader = loadShader(filename);
            shaderFile = dataFile(filename);
            String filePath = shaderFile.getPath();
            lastChecked = currentTimeMillis();
            if (shaderFile.isFile()) {
                //println(filePath + " was found and is now being checked for changes every " + refreshRateInMillis + " ms.");
            } else {
                println("Could not find shader at " + filePath + ", please adjust the actual file path");
            }
            this.path = filename;
        }

        void update(boolean filter) {
            long currentTimeMillis = currentTimeMillis();
            if (currentTimeMillis < lastChecked + refreshRateInMillis) {
                return;
            }
            lastChecked = currentTimeMillis;
            long lastModified = shaderFile.lastModified();
            if (lastModified > lastKnownModified && lastModified > lastKnownUncompilable) {
                try {
                    PShader recentCandidate = loadShader(path);
                    // we need to call filter() or shader() here in order to catch any compilation errors and not halt the sketch
                    if (filter) {
                        filter(recentCandidate);
                    } else {
                        shader(recentCandidate);
                    }
                    compiledShader = recentCandidate;
                    lastKnownModified = lastModified;
                } catch (Exception ex) {
                    lastKnownUncompilable = lastModified;
                    println(ex.getMessage());
                }
            }
        }
    }
}
