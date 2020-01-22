uniform vec2 resolution;
uniform float time;

const int steps = 50;
const float stepSize = 0.1;


void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 origin = vec3(uv, -1.);
    vec3 dir = vec3(0, 0, 1);
    vec3 p;
    float d = 0.;
    for (int i = 0; i < steps; i++){
        p = origin+i*dir;
        d += length(p)-1;
    }
    vec3 color = vec3(d);
    gl_FragColor = vec4(color, 1.);
}
