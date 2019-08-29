package applet;

import processing.opengl.PShader;

import java.io.File;
import java.util.ArrayList;

import static java.lang.System.currentTimeMillis;

public class HotswapGuiSketch extends GuiSketch {

    public void setup(){

    }

    public void draw(){

    }

    ArrayList<ShaderSnapshot> snapshots = new ArrayList<ShaderSnapshot>();
    int refreshRateInMillis = 100;

    public PShader uniform(String path) {
        ShaderSnapshot snapshot = findSnapshotByPath(path);
        snapshot = initIfNull(snapshot, path);
        return snapshot.compiledShader;
    }

    public void hotFilter(String path) {
        hotShader(path, true);
    }

    public void hotShader(String path) {
        hotShader(path, false);
    }

    private void hotShader(String path, boolean filter) {
        ShaderSnapshot snapshot = findSnapshotByPath(path);
        snapshot = initIfNull(snapshot, path);
        snapshot.update(filter);
    }

    private ShaderSnapshot initIfNull(ShaderSnapshot snapshot, String path) {
        if (snapshot == null) {
            snapshot = new ShaderSnapshot(path);
            snapshots.add(snapshot);
        }
        return snapshot;
    }

    private ShaderSnapshot findSnapshotByPath(String path) {
        for (ShaderSnapshot snapshot : snapshots) {
            if (snapshot.path.equals(path)) {
                return snapshot;
            }
        }
        return null;
    }

    private class ShaderSnapshot {
        String path;
        PShader compiledShader;
        File shaderFile;
        long lastKnownModified = -refreshRateInMillis;
        long lastKnownUncompilable = -refreshRateInMillis;
        long lastChecked = -refreshRateInMillis;

        ShaderSnapshot(String filename) {
            compiledShader = loadShader(filename);
            shaderFile = dataFile(filename);
            lastKnownModified = shaderFile.lastModified();
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
                applyShader(compiledShader, filter);
                return;
            }
            lastChecked = currentTimeMillis;
            long lastModified = shaderFile.lastModified();
            if (lastModified > lastKnownModified && lastModified > lastKnownUncompilable) {
                try {
                    PShader recentCandidate = loadShader(path);
                    // we need to call filter() or shader() here in order to catch any compilation errors and not halt the sketch
                    applyShader(recentCandidate, filter);
                    compiledShader = recentCandidate;
                    lastKnownModified = lastModified;
                }
                catch (Exception ex) {
                    lastKnownUncompilable = lastModified;
                    println(ex.getMessage());
                }
            } else{
                applyShader(compiledShader, filter);
            }
        }

        private void applyShader(PShader shader, boolean filter) {
            if (filter) {
                filter(shader);
            } else {
                shader(shader);
            }
        }

    }
}
