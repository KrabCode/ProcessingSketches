
uniform sampler2D a, b;
uniform vec2 resolution;
uniform float damp;

vec3 get(sampler2D tex, vec2 offset){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    offset /= resolution.xy;
    return texture2D(tex, uv+offset).rgb;
}

vec3 wave(){
    vec3 col = (get(a, vec2(-1, 0)) +
        get(a, vec2(1, 0)) +
        get(a, vec2(0, 1)) +
        get(a, vec2(0, -1))) /2
        - get(b, vec2(0)
        );
    col *= damp;
    return col;
}

void main(){
    vec3 col = wave();
    gl_FragColor = vec4(col, 1.);
}