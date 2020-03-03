uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform int colorCount;
uniform vec4 hsba_0;
uniform vec4 hsba_1;
uniform vec4 hsba_2;
uniform vec4 hsba_3;
uniform vec4 hsba_4;
uniform vec4 hsba_5;
uniform vec4 hsba_6;
uniform vec4 hsba_7;
uniform vec4 hsba_8;
uniform vec4 hsba_9;

vec3 rgb(in vec3 hsb){
    vec3 rgb = clamp(abs(mod(hsb.x*6.0+
    vec3(0.0, 4.0, 2.0), 6.0)-3.0)-1.0, 0.0, 1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);
    return hsb.z * mix(vec3(1.0), rgb, hsb.y);
}

float map(float value, float start1, float stop1, float start2, float stop2){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

vec4 getColor(float pct){
    pct = fract(pct);
    float colorPct = clamp(map(pct, 0, 1, 0, colorCount-1), 0, colorCount-1);
    int previousColorIndex = int(floor(colorPct));
    float lerpToNextColor = fract(colorPct);
    vec4[] colors = vec4[](
        hsba_0, hsba_1, hsba_2, hsba_3, hsba_4, hsba_5, hsba_6, hsba_7, hsba_8, hsba_9);
    vec4 prevColor = colors[previousColorIndex];
    vec4 nextColor = colors[previousColorIndex+1];
    prevColor.rgb = rgb(prevColor.rgb);
    nextColor.rgb = rgb(nextColor.rgb);
    return mix(prevColor, nextColor, lerpToNextColor);
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec4 col = getColor(1.-uv.y);
    gl_FragColor = col;
}