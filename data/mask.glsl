uniform sampler2D texture;
uniform sampler2D mask;
uniform vec2 resolution;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 origTex = texture(texture, uv).rgb;
    vec3 maskTex = texture(mask, uv).rgb;
    vec3 color = origTex - (1.-maskTex);
    gl_FragColor = vec4(color, 1.);
}