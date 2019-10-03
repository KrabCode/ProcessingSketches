#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float d = length(uv*3.);
    vec3 col = 0.5 + 0.5*cos(time+d+vec3(0,2,4));
    gl_FragColor = vec4(col, 1.);
}