#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 col = vec3(88./255., 20./255., 185./255.);
    float sun = 1.-length(uv);
    float sunSize = .999995;
    float sunTransition = .2;
    col += smoothstep(sunSize-sunTransition, sunSize, sun);
    col -= length(uv);
    gl_FragColor = vec4(col, 1.);
}