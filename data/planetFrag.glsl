
#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 vertColor;
varying vec4 vertCoord;
uniform vec2 resolution;
uniform float time;

void main() {
    vec2 uv = (vertCoord.xy-.5*resolution) / resolution.y;
    vec3 col = 0.5 + 0.5*cos(time+uv.xyx+vec3(0,2,4));
    gl_FragColor = vec4(col, 1.);
}