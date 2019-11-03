#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 col = texture(texture, uv).rgb;
    col = smoothstep(0.0, 1.0, col);
//    col -= step(1.-length(cv), .5);
    gl_FragColor = vec4(col, 1.);
}