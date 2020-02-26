uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

#define e 1.618282
#define pi 3.14159

// beautiful sdf visualisation from here: https://www.shadertoy.com/view/XsyGRW
vec3 draw_line(float d, float thickness) {
    const float aa = 3.0;
    return vec3(smoothstep(0.0, aa / resolution.y, max(0.0, abs(d) - thickness)));
}

vec3 draw_line(float d) {
    return draw_line(d, 0.0025);
}

float draw_solid(float d) {
    return smoothstep(0.0, 3.0 / resolution.y, max(0.0, d));
}

vec3 draw_distance(float d, vec2 p) {
    float t = clamp(d * 0.85, 0.0, 1.0);
    vec3 grad = mix(vec3(1, 0.8, 0.5), vec3(0.3, 0.8, 1), t);
    float d0 = abs(1.0 - draw_line(mod(d + 0.1, 0.2) - 0.1).x);
    float d1 = abs(1.0 - draw_line(mod(d + 0.025, 0.05) - 0.025).x);
    float d2 = abs(1.0 - draw_line(d).x);
    vec3 rim = vec3(max(d2 * 0.85, max(d0 * 0.25, d1 * 0.06125)));
    grad -= rim;
    grad -= mix(vec3(0.05, 0.35, 0.35), vec3(0.0), draw_solid(d));
    return grad;
}

float map(float x, float a1, float a2, float b1, float b2){
    return b1 + (b2-b1) * (x-a1) / (a2-a1);
}

float sdCircle( vec2 p, float r ){
    return length(p) - r;
}

float sdBox( in vec2 p, in vec2 b ){
    vec2 d = abs(p)-b;
    return length(max(d,0.0)) + min(max(d.x,d.y),0.0);
}

void main(){
    float t = time*.2;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    cv *= 2.;
    float radius = .25;
    float centerCircle = length(cv)-radius;
    float dist = centerCircle;
    int count = 10;
    for(int i = 0; i <= count; i++){
        float a = map(i, 0, count, 0, pi*2.);
        vec2 pos = vec2(sin(a+t), cos(a+t));
        float travelCircle = length(cv-.5*pos)-radius*.15;
        dist = min(dist, travelCircle);
    }

    vec3 color = draw_distance(dist, cv);
    gl_FragColor = vec4(color, 1.);
}