#version 120

uniform sampler2D texture;
uniform vec2 resolution;

void main() {
    vec2 uv = gl_FragCoord.xy/resolution.xy;
    float a = texture2D(texture, uv).r + texture2D(texture, uv).g + texture2D(texture, uv).b;
    gl_FragColor = vec4(vec3(1), 1.-a);
}
