uniform sampler2D texture;
uniform sampler2D img;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    uv.y = 1.-uv.y;
    float md = max(abs(cv.x), abs(cv.y));
    for(int i = 1; i < 5; i++){
        float s = step(md, 0.5-.1*i);
        uv.y *= 1.0 + 0.05 * s;
    }
    vec3 col = texture(img, uv).rgb;
    gl_FragColor = vec4(col, 1.);
}