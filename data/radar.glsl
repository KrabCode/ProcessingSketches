#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

#define pi 3.14159265359

vec3 rgb(float h, float s, float b){
    vec3 c = vec3(h, s, b);
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0, 4.0, 2.0), 6.0)-3.0)-1.0, 0.0, 1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);  return c.z * mix(vec3(1.0), rgb, c.y);
}

mat2 rotate2d(float angle){
    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
}

float angularDiameter(float r, float size) {
    return atan(2 * (size / (2 * r)));
}

float cubicPulse(float c, float w, float x){
    x = abs(x - c);
    if (x>w) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

float map(float value, float start1, float stop1, float start2, float stop2){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

float norm(float value, float start, float stop){
    return map(value, start, stop, 0., 1.);
}

void main(){
    float t = time;
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 darkerBlue = rgb(.6, 1., 0.35);
    vec3 lighterBlue = rgb(.6, 0.9, 1.);
    vec3 col = vec3(0);

    float d = length(uv);
    float w = .006;
    float r = smoothstep(.4+w, .4-w, d);
    float sharpR = step(d, .4);

    vec3 bb = texture2D(texture, gl_FragCoord.xy/resolution.xy).rgb;
    col = mix(bb, col, 0.1);
    col -= .001;

    float staticAngle = atan(uv.y, uv.x);
    uv *= rotate2d(t);
    float rotatingAngle = atan(uv.y, uv.x);

//TODO find distance from rotating angle, normalize it, expose some noise blobs on the radar using it

    col = max(col, darkerBlue * cubicPulse(0, 0.2, sin(d*70)) * sharpR);
    col = max(col, darkerBlue * cubicPulse(.0, angularDiameter(d,.05), sin(staticAngle*12.)) * r);
    col = max(col, lighterBlue * cubicPulse(.0, angularDiameter(d,.005), rotatingAngle) * r);
    gl_FragColor = vec4(col, 1.);
}