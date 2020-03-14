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
//    pct = fract(pct);
    float colorPct = clamp(map(pct, 0, 1, 0, colorCount-1), 0, colorCount-1);
    int previousColorIndex = int(floor(colorPct));
    float lerpAmount = fract(colorPct);
    vec4[] colors = vec4[](hsba_0, hsba_1, hsba_2, hsba_3, hsba_4, hsba_5, hsba_6, hsba_7, hsba_8, hsba_9);
    vec4 prevColor = colors[previousColorIndex];
    vec4 nextColor = colors[previousColorIndex+1];
    prevColor.rgb = rgb(prevColor.rgb);
    nextColor.rgb = rgb(nextColor.rgb);
    return mix(prevColor, nextColor, lerpAmount);
}


// Precision-adjusted variations of https://www.shadertoy.com/view/4djSRW
float hash(float p) { p = fract(p * 0.011); p *= p + 7.5; p *= p + p; return fract(p); }
float hash(vec2 p) {vec3 p3 = fract(vec3(p.xyx) * 0.13); p3 += dot(p3, p3.yzx + 3.333); return fract((p3.x + p3.y) * p3.z); }

float noise(float x) {
    float i = floor(x);
    float f = fract(x);
    float u = f * f * (3.0 - 2.0 * f);
    return mix(hash(i), hash(i + 1.0), u);
}


float noise(vec2 x) {
    vec2 i = floor(x);
    vec2 f = fract(x);

    // Four corners in 2D of a tile
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    // Simple 2D lerp using smoothstep envelope between the values.
    // return vec3(mix(mix(a, b, smoothstep(0.0, 1.0, f.x)),
    //			mix(c, d, smoothstep(0.0, 1.0, f.x)),
    //			smoothstep(0.0, 1.0, f.y)));

    // Same code, with the clamps in smoothstep and common subexpressions
    // optimized away.
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float noise(vec3 x) {
    const vec3 step = vec3(110, 241, 171);

    vec3 i = floor(x);
    vec3 f = fract(x);

    // For performance, compute the base input to a 1D hash from the integer part of the argument and the
    // incremental change to the 1D based on the 3D -> 1D wrapping
    float n = dot(i, step);

    vec3 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix( hash(n + dot(step, vec3(0, 0, 0))), hash(n + dot(step, vec3(1, 0, 0))), u.x),
    mix( hash(n + dot(step, vec3(0, 1, 0))), hash(n + dot(step, vec3(1, 1, 0))), u.x), u.y),
    mix(mix( hash(n + dot(step, vec3(0, 0, 1))), hash(n + dot(step, vec3(1, 0, 1))), u.x),
    mix( hash(n + dot(step, vec3(0, 1, 1))), hash(n + dot(step, vec3(1, 1, 1))), u.x), u.y), u.z);
}

float fbm(vec3 x){
    float freq = 1.;
    float amp = 1.;
    float sum = 0;
    for(int i = 0; i < 8; i++){
        sum += amp*(1.-2.*noise(x*freq));
        amp *= .5;
        freq *= 1.5;
        x += 13.124 + freq * amp;
    }
    return .5+.5*sum;
}

float fbm(vec2 cv, float t){
    return fbm(vec3(cv, t));
}

float cubicPulse( float c, float w, float x ){
    x = abs(x - c);
    if( x>w ) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float t = time*.5;
    vec4 col = getColor(1.-uv.y);
    float yScale = 103 + (cv.y) *77;
    float n = .5*fbm(vec3(cv.y * yScale + t, cv.x*.23, t));
    float lit = n*smoothstep(.05, -.05, cv.y);;
    col.r += lit;
    col.g += lit*.5;
    gl_FragColor = col;
}