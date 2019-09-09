#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 ov = gl_FragCoord.xy / resolution;
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 tex = texture(texture, ov).rgb;
    vec3 col = tex*1.0;
    gl_FragColor = vec4(tex, 1.);
}