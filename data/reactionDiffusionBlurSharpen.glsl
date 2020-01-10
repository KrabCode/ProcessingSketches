#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform int passIndex;
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;


vec4 sharpen(sampler2D channel, vec2 uv, vec2 res){
    vec2 step = 1.0 / res;
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

vec4 blur(sampler2D channel, vec2 uv, vec2 res){
    vec2 step = 1.0 / res;
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


void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float d = 1.*length(cv);
    float angle = atan(cv.y, cv.x)+d;
    float mag = -0*sin(d);
    float r = mag/resolution.x;
    uv += vec2(r*cos(angle), r*sin(angle));
    vec4 col;
    if (mod(passIndex, 2) == 0){
        col = blur(texture, uv, resolution);
    } else {
        col = sharpen(texture, uv, resolution);
    }
    gl_FragColor = col;
}