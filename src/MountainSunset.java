import applet.Sketch;
import peasy.PeasyCam;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShader;

@SuppressWarnings("DuplicatedCode")
public class MountainSunset extends Sketch {
    public static void main(String[] args) {
        Sketch.main("MountainSunset");
    }

    float t;
    PeasyCam cam;
    PVector lightDir = new PVector();
    PShader defaultShader;
    PGraphics shadowMap;

    float baseWidth;
    float baseDepth;
    float maxAltitude;

    float detail = 60;
    float[][] fbmGrid = new float[floor(detail)][floor(detail)];


    public void settings() {
        size(800, 800, P3D);
    }

    public void setup() {
        super.setup();
        cam = new PeasyCam(this, 150);
        initDefaultPass();
        initShadowPass();
        baseWidth = 200;
        baseDepth = 200;
        maxAltitude = 100;
    }

    public void draw() {
        super.draw();
        t = radians(frameCount);
        if (button("reset gui")) {
            resetGui();
        }
        if (button("reset seed")) {
            noiseSeed(millis());
        }
        float oldDetail = detail;
        detail = slider("detail", 300);
        if(detail != oldDetail){
            resetFbmGrid();
        }
        lightDir.set(maxAltitude*sin(t),-maxAltitude*.2f,maxAltitude*cos(t));
        shadowMap.beginDraw();
        shadowMap.camera(lightDir.x, lightDir.y, lightDir.z, 0, 0, 0, 0, 1, 0);
        shadowMap.background(0xffffffff);
        landscape(shadowMap);
        shadowMap.endDraw();
        shadowMap.updatePixels();
        updateDefaultShader();

        background(35, 57, 109);
        landscape(g);

        resetShader();
        noLights();
        gui();
    }

    void landscape(PGraphics canvas) {
        float logicalCenter = (detail - 1) / 2f;
        float maxDistFromLogicalCenter = detail * .5f;
        canvas.pushMatrix();
        canvas.translate(0, maxAltitude/2);
        canvas.noStroke();
        canvas.fill(0);
        for (int zIndex = 0; zIndex < detail; zIndex++) {
            canvas.beginShape(TRIANGLE_STRIP);
            for (int xIndex = 0; xIndex < detail; xIndex++) {
                float x = map(xIndex, 0, detail - 1, -baseWidth * .5f, baseWidth * .5f);
                float z0 = map(zIndex, 0, detail - 1, -baseDepth * .5f, baseDepth * .5f);
                float z1 = map(zIndex + 1, 0, detail - 1, -baseDepth * .5f, baseDepth * .5f);
                float d0 = 1 - constrain(map((dist(xIndex, zIndex, logicalCenter, logicalCenter)), 0, maxDistFromLogicalCenter, 0, 1), 0, 1);
                float d1 = 1 - constrain(map((dist(xIndex, zIndex + 1, logicalCenter, logicalCenter)), 0, maxDistFromLogicalCenter, 0, 1), 0, 1);
                float n0, n1;
                if(toggle("lockFbm", true)){
                    n0 = getFbmAt(xIndex, zIndex);
                    n1 = getFbmAt(xIndex, zIndex + 1);
                }else{
                    n0 = fbm(xIndex, zIndex);
                    n1 = fbm(xIndex, zIndex + 1);
                }
                float y0 = -d0 * maxAltitude + maxAltitude * n0;
                float y1 = -d1 * maxAltitude + maxAltitude * n1;
                canvas.fill(y0 < -maxAltitude/2 ? 255 : 255*d0);
                canvas.normal(x, y0, z0);
                canvas.vertex(x, y0, z0);
                canvas.fill(y1 < -maxAltitude/2 ? 255 : 255*d1);
                canvas.normal(x, y1, z1);
                canvas.vertex(x, y1, z1);
            }
            canvas.endShape(TRIANGLE_STRIP);
        }
        canvas.translate(lightDir.x, lightDir.y, lightDir.z);
        canvas.fill(255);
        canvas.sphere(5);
        canvas.popMatrix();

    }

    void resetFbmGrid(){
        fbmGrid = new float[ceil(detail)][ceil(detail)];
        for(int i = 0; i < detail; i++){
            for(int j = 0; j < detail; j++){
                fbmGrid[i][j] = -1;
            }
        }
    }

    float getFbmAt(int x, int y){
        if(x < 0 || x >= detail || y < 0 || y >= detail){
            return 0;
        }
        float val = fbmGrid[x][y];
        if(val == -1){
            val = fbm(x,y);
            fbmGrid[x][y] = val;
        }
        return val;
    }


    float fbm(float x, float y) {
        float sum = 0;
        float frq = slider("freq", 0, 1, .05f);
        float amp = slider("amp", 0,1,.4f);
        for (int i = 0; i < 6; i++) {
            sum += amp * (-1 + 2 * noise(x * frq, y * frq));
            frq *= slider("frq mod", 0, 5, 1.4f);
            amp *= slider("amp mod", .5f);
            x += 50;
            y += 50;
        }
        return abs(sum);
    }

    public void initShadowPass() {
        shadowMap = createGraphics(2048, 2048, P3D);
        String[] vertSource = {
                "uniform mat4 transform;",

                "attribute vec4 vertex;",

                "void main() {",
                "gl_Position = transform * vertex;",
                "}"
        };
        String[] fragSource = {

                // In the default shader we won't be able to access the shadowMap's depth anymore,
                // just the color, so this function will pack the 16bit depth float into the first
                // two 8bit channels of the rgba vector.
                "vec4 packDepth(float depth) {",
                "float depthFrac = fract(depth * 255.0);",
                "return vec4(depth - depthFrac / 255.0, depthFrac, 1.0, 1.0);",
                "}",

                "void main(void) {",
                "gl_FragColor = packDepth(gl_FragCoord.z);",
                "}"
        };
        shadowMap.noSmooth(); // Antialiasing on the shadowMap leads to weird artifacts
//        shadowMap.loadPixels(); // Will interfere with noSmooth() (probably a bug in Processing)
        shadowMap.beginDraw();
        shadowMap.noStroke();
        shadowMap.shader(new PShader(this, vertSource, fragSource));
        shadowMap.ortho(-200, 200, -200, 200, 10, 400); // Setup orthogonal view matrix for the directional light
        shadowMap.endDraw();
    }

    public void initDefaultPass() {
        String[] vertSource = {
                "uniform mat4 transform;",
                "uniform mat4 modelview;",
                "uniform mat3 normalMatrix;",
                "uniform mat4 shadowTransform;",
                "uniform vec3 lightDirection;",

                "attribute vec4 vertex;",
                "attribute vec4 color;",
                "attribute vec3 normal;",

                "varying vec4 vertColor;",
                "varying vec4 shadowCoord;",
                "varying float lightIntensity;",

                "void main() {",
                "vertColor = color;",
                "vec4 vertPosition = modelview * vertex;", // Get vertex position in model view space
                "vec3 vertNormal = normalize(normalMatrix * normal);", // Get normal direction in model view space
                "shadowCoord = shadowTransform * (vertPosition + vec4(vertNormal, 0.0));", // Normal bias removes the shadow acne
                "lightIntensity = 0.5 + dot(-lightDirection, vertNormal) * 0.5;",
                "gl_Position = transform * vertex;",
                "}"
        };
        String[] fragSource = {
                "#version 120",

                // Used a bigger poisson disk kernel than in the tutorial to get smoother results
                "const vec2 poissonDisk[9] = vec2[] (",
                "vec2(0.95581, -0.18159), vec2(0.50147, -0.35807), vec2(0.69607, 0.35559),",
                "vec2(-0.0036825, -0.59150), vec2(0.15930, 0.089750), vec2(-0.65031, 0.058189),",
                "vec2(0.11915, 0.78449), vec2(-0.34296, 0.51575), vec2(-0.60380, -0.41527)",
                ");",

                // Unpack the 16bit depth float from the first two 8bit channels of the rgba vector
                "float unpackDepth(vec4 color) {",
                "return color.r + color.g / 255.0;",
                "}",

                "uniform sampler2D shadowMap;",

                "varying vec4 vertColor;",
                "varying vec4 shadowCoord;",
                "varying float lightIntensity;",

                "void main(void) {",

                // Project shadow coords, needed for a perspective light matrix (spotlight)
                "vec3 shadowCoordProj = shadowCoord.xyz / shadowCoord.w;",

                // Only render shadow if fragment is facing the light
                "if(lightIntensity > 0.5) {",
                "float visibility = 9.0;",

                // I used step() instead of branching, should be much faster this way
                "for(int n = 0; n < 9; ++n)",
                "visibility += step(shadowCoordProj.z, unpackDepth(texture2D(shadowMap, shadowCoordProj.xy + poissonDisk[n] / 512.0)));",

                "gl_FragColor = vec4(vertColor.rgb * min(visibility * 0.05556, lightIntensity), vertColor.a);",
                "} else",
                "gl_FragColor = vec4(vertColor.rgb * lightIntensity, vertColor.a);",

                "}"
        };
        noStroke();
        defaultShader = new PShader(this, vertSource, fragSource);
        perspective(60 * DEG_TO_RAD, (float) width / height, 10, 1000);
    }

    void updateDefaultShader() {
        // Bias matrix to move homogeneous shadowCoords into the UV texture space
        PMatrix3D shadowTransform = new PMatrix3D(
                0.5f, 0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.0f, 0.5f,
                0.0f, 0.0f, 0.5f, 0.5f,
                0.0f, 0.0f, 0.0f, 1.0f
        );

        // Apply project modelview matrix from the shadow pass (light direction)
        shadowTransform.apply(((PGraphicsOpenGL) shadowMap).projmodelview);

        // Apply the inverted modelview matrix from the default pass to get the original vertex
        // positions inside the shader. This is needed because Processing is pre-multiplying
        // the vertices by the modelview matrix (for better performance).
        PMatrix3D modelviewInv = ((PGraphicsOpenGL) g).modelviewInv;
        shadowTransform.apply(modelviewInv);

        // Convert column-minor PMatrix to column-major GLMatrix and send it to the shader.
        // PShader.set(String, PMatrix3D) doesn't convert the matrix for some reason.
        defaultShader.set("shadowTransform", new PMatrix3D(
                shadowTransform.m00, shadowTransform.m10, shadowTransform.m20, shadowTransform.m30,
                shadowTransform.m01, shadowTransform.m11, shadowTransform.m21, shadowTransform.m31,
                shadowTransform.m02, shadowTransform.m12, shadowTransform.m22, shadowTransform.m32,
                shadowTransform.m03, shadowTransform.m13, shadowTransform.m23, shadowTransform.m33
        ));

        // Calculate light direction normal, which is the transpose of the inverse of the
        // modelview matrix and send it to the default shader.
        float lightNormalX = lightDir.x * modelviewInv.m00 + lightDir.y * modelviewInv.m10 + lightDir.z * modelviewInv.m20;
        float lightNormalY = lightDir.x * modelviewInv.m01 + lightDir.y * modelviewInv.m11 + lightDir.z * modelviewInv.m21;
        float lightNormalZ = lightDir.x * modelviewInv.m02 + lightDir.y * modelviewInv.m12 + lightDir.z * modelviewInv.m22;
        float normalLength = sqrt(lightNormalX * lightNormalX + lightNormalY * lightNormalY + lightNormalZ * lightNormalZ);
        defaultShader.set("lightDirection", lightNormalX / -normalLength, lightNormalY / -normalLength, lightNormalZ / -normalLength);

        // Send the shadowmap to the default shader
        defaultShader.set("shadowMap", shadowMap);

        shader(defaultShader);
    }
}
