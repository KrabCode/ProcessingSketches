#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform bool blurOrSharpen;
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform float baseSharpen;
uniform float offSharpen;
uniform float baseBlur;
uniform float offBlur;
uniform float dEase;

vec4 sharpen(sampler2D channel, vec2 uv, vec2 res, float mag){
    vec2 step = mag / res;
    float kernel [9];vec2 offset [9];


    offset[0] = vec2(-step.x, -step.y);
    offset[1] = vec2(0.0, -step.y);
    offset[2] = vec2(step.x, -step.y);

    offset[3] = vec2(-step.x, 0.0);
    offset[4] = vec2(0.0, 0.0);
    offset[5] = vec2(step.x, 0.0);

    offset[6] = vec2(-step.x, step.y);
    offset[7] = vec2(0.0, step.y);
    offset[8] = vec2(step.x, step.y);

    kernel[0] = 0.0; kernel[1] = -0.25; kernel[2] = 0.0;
    kernel[3] = -0.25; kernel[4] = 1.0; kernel[5] = -0.25;
    kernel[6] = 0.0; kernel[7] = -0.25; kernel[8] = 0.0;

    vec4 sum = texture(channel, uv);

    for (int i = 0; i < 9; i++) {
        vec4 color = texture(channel, uv + offset[i]);
        sum += color * kernel[i]*2.;
    }
    return sum;
}

vec4 blur(sampler2D channel, vec2 uv, vec2 res, float mag){
    vec2 step = mag / res;
    float kernel [9];vec2 offset [9];


    offset[0] = vec2(-step.x, -step.y);
    offset[1] = vec2(0.0, -step.y);
    offset[2] = vec2(step.x, -step.y);

    offset[3] = vec2(-step.x, 0.0);
    offset[4] = vec2(0.0, 0.0);
    offset[5] = vec2(step.x, 0.0);

    offset[6] = vec2(-step.x, step.y);
    offset[7] = vec2(0.0, step.y);
    offset[8] = vec2(step.x, step.y);

    kernel[0] = 1.0; kernel[1] = 1.; kernel[2] = 1.0;
    kernel[3] = 1.; kernel[4] = 1.0; kernel[5] = 1.;
    kernel[6] = 1.0; kernel[7] = 1.; kernel[8] = 1.0;

    vec4 sum = vec4(0);

    for (int i = 0; i < 9; i++) {
        vec4 color = texture(channel, uv + offset[i]);
        sum += color * kernel[i];
    }
    sum /= 9.;

    return sum;
}

float ease(float p, float g) {
    if (p < 0.5) return 0.5f * pow(2 * p, g);
    return 1 - 0.5f * pow(2 * (1 - p), g);
}

float map(float value, float start1, float stop1, float start2, float stop2){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float d = length(cv)*2.;
    float s = map(d, 0, 1, baseSharpen, baseSharpen+offSharpen);
    float b = map(d, 0, 1, baseBlur, baseBlur+offBlur);
    vec4 col;
    if (blurOrSharpen){
        col = blur(texture, uv, resolution, b);
    } else {
        col = sharpen(texture, uv, resolution, s);
    }
    gl_FragColor = col;
}