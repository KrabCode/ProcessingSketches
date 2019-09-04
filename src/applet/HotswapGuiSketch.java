package applet;

import processing.core.PGraphics;
import processing.opengl.PShader;

import java.io.File;
import java.util.ArrayList;

import static java.lang.System.currentTimeMillis;

/**
 * A sketch extending this class can apply changes to shaders as you edit the shader file
 *
 * - use uniform() to get a reference to the shader file in order to pass uniforms to it
 * - use hotFilter() and hotShader() to apply your last compilable shader as filter or shader respectively
 * - no need to call loadShader() manually at all
 *
 * - you have to actually change the last modified timestamp of the file, (try CTRL+S)
 * - the results of any compilation errors will be printed to standard processing console
 * - only supports fragment shaders
 *
 * TODO support vertex shaders too, refresh when either file gets updated
 */
public abstract class HotswapGuiSketch extends GuiSketch {

    ArrayList<ShaderSnapshot> snapshots = new ArrayList<ShaderSnapshot>();
    int refreshRateInMillis = 20;

    public PShader uniform(String path) {
        ShaderSnapshot snapshot = findSnapshotByPath(path);
        snapshot = initIfNull(snapshot, path);
        return snapshot.compiledShader;
    }


    public void hotFilter(String path, PGraphics canvas) {
        hotShader(path, true, canvas);
    }

    public void hotShader(String path, PGraphics canvas) {
        hotShader(path, false, canvas);
    }

    public void hotFilter(String path) {
        hotShader(path, true, g);
    }

    public void hotShader(String path) {
        hotShader(path, false, g);
    }

    private void hotShader(String path, boolean filter, PGraphics canvas) {
        ShaderSnapshot snapshot = findSnapshotByPath(path);
        snapshot = initIfNull(snapshot, path);
        snapshot.update(filter, canvas);
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
        boolean compiledAtLeastOnce = false;
        long lastKnownModified, lastChecked;
        long lastKnownUncompilable = -refreshRateInMillis;


        ShaderSnapshot(String filename) {
            compiledShader = loadShader(filename);
            shaderFile = dataFile(filename);
            lastKnownModified = shaderFile.lastModified();
            String filePath = shaderFile.getPath();
            lastChecked = currentTimeMillis();
            if (shaderFile.isFile()) {
//                println(shaderFile.getName() + " registered successfully");
            } else {
                println("Could not find shader at " + filePath + ", please adjust the actual file path");
            }
            this.path = filename;
        }

        void update(boolean filter, PGraphics pg) {
            long currentTimeMillis = currentTimeMillis();
            long lastModified = shaderFile.lastModified();
            if (compiledAtLeastOnce && currentTimeMillis < lastChecked + refreshRateInMillis) {
                applyShader(compiledShader, filter, pg);
                return;
            }
            if(!compiledAtLeastOnce && lastModified > lastKnownUncompilable){
                tryCompileNewVersion(filter, pg, lastModified);
                return;
            }
            lastChecked = currentTimeMillis;
            if (lastModified > lastKnownModified && lastModified > lastKnownUncompilable) {
                tryCompileNewVersion(filter, pg, lastModified);
            } else if(compiledAtLeastOnce) {
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

        private void tryCompileNewVersion(boolean filter, PGraphics pg, long lastModified){
            try {
                PShader recentCandidate = loadShader(path);
                // we need to call filter() or shader() here in order to catch any compilation errors and not halt the sketch
                applyShader(recentCandidate, filter, pg);
                compiledShader = recentCandidate;
                compiledAtLeastOnce = true;
                lastKnownModified = lastModified;
            } catch (Exception ex) {
                lastKnownUncompilable = lastModified;
                println("\n" + shaderFile.getName() + ": " + ex.getMessage());
            }
        }
    }
}