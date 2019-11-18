#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
varying vec4 vertTexCoord;
uniform vec2 resolution;
uniform float time;
uniform float delta;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 dir = vertTexCoord.xy - vec2(.5);
    float d = length(dir);
    vec2 value = d * dir * delta;

    vec4 c1 = texture2D(texture, vertTexCoord.xy - value / resolution.x);
    vec4 c2 = texture2D(texture, vertTexCoord.xy);
    vec4 c3 = texture2D(texture, vertTexCoord.xy + value / resolution.y);

    gl_FragColor = vec4(
        max(max(c1.r, c2.r), c3.r),
        max(max(c1.g, c2.g), c3.g),
        max(max(c1.b, c2.b), c3.b),
        c2.a);
}