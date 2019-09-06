#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 col = vec3(.35, .4, 1.);
    float sun = 1.-length(uv);
    float sunSize = .95;
    float sunTransition = .05;
    col += smoothstep(sunSize-sunTransition, sunSize, sun);
    col -= length(uv);
    gl_FragColor = vec4(col, 1.);
}