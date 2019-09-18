#version 120
#define pi 3.14159265359

uniform float time;
uniform vec2 resolution;
uniform sampler2D texture;

float cubicPulse(float c, float w, float x){
    x = abs(x - c);
    if (x>w) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

vec3 rgb( in vec3 hsb){
    vec3 rgb = clamp(abs(mod(hsb.x*6.0+
    vec3(0.0,4.0,2.0),6.0)-3.0)-1.0,0.0,1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);
    return hsb.z * mix(vec3(1.0), rgb, hsb.y);
}

void main() {
    float t = time*10.;
    vec2 uv = gl_FragCoord.xy/resolution.xy;
    vec2 cc = (gl_FragCoord.xy-.5*resolution.x)/resolution.y;
    vec3 new = vec3(0.);

    float d = length(cc);
    float a = atan(cc.y,cc.x);
    float r = .2+.02*sin(a*8.+t);
    float pct = cubicPulse(r, 0.01, d);
    new.r += pct;

    float alpha = .1;
    vec3 old = texture2D(texture, uv).rgb;
    vec3 composite = mix(old, new, alpha);
    gl_FragColor = vec4(composite, 1.);
}
