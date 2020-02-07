uniform vec2 resolution;
uniform sampler2D texture;
uniform float smoothstepStart;
uniform float smoothstepEnd;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 tex = texture2D(texture, uv).rgb;
    tex = smoothstep(smoothstepStart, smoothstepEnd, tex);
    gl_FragColor = vec4(tex, 1);
}
