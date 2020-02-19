uniform sampler2D texture;
uniform sampler2D noise;
uniform vec2 resolution;
uniform float time;

vec2 wrapAround(vec2 p){
    return mod(p, vec2(1));
}

float texNoise(vec2 xy){
    return texture2D(noise, wrapAround(xy)).r;
}

float fbm(vec2 xy){
    float sum = 0.;
    float amp = 1.0;
    float freq = 0.01;
    for(int i = 0; i < 8.; i++){
        sum += amp*(1-2*(texNoise(xy*freq)));
        freq *= 2.;
        amp *= .3;
        xy += sum*3.;
    }
    return .5+.5*sum;
}

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float t = time*.5;
    float d = 5.*max(abs(cv.x) , abs(cv.y));
    float a = 0.8*abs(cos(atan(cv.y, cv.x)*4.));
    float n = fbm(vec2(a, d));
    n = smoothstep(0.2, 0.8, n);
    vec3 color = vec3(n);
    gl_FragColor = vec4(color, 1.);
}