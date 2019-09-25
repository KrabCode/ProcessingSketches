#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    float t = time;
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 col = vec3(.0,0,0.3);
    gl_FragColor = vec4(col, 1.);
}