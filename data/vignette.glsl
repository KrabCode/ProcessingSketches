uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform float startRadius;
uniform float endRadius;

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = gl_FragCoord.xy / resolution,xy;
    vec3 tex = texture(texture, uv).rgb;
    float d = length(cv);
    vec3 color = tex*(1.-smoothstep(startRadius, endRadius, d));
    gl_FragColor = vec4(color, 1.);
}