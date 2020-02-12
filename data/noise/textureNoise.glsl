precision highp float;

uniform sampler2D rgbNoise;
uniform vec2 resolution;
uniform float time;

//TODO make it work!

// courtesy of Inigo Quilez
// https://www.shadertoy.com/view/XslGRr
float noise(vec3 x){
    vec3 p = floor(x);
    vec3 f = fract(x);
    f = f*f*(3.0-2.0*f);
    ivec3 q = ivec3(p);
    ivec2 uv = q.xy + ivec2(37,239)*q.z;
    vec2 rg = mix(mix(texelFetch(rgbNoise,(uv)&255,0),
    texelFetch(rgbNoise,(uv+ivec2(1,0))&255,0),f.x),
    mix(texelFetch(rgbNoise,(uv+ivec2(0,1))&255,0),
    texelFetch(rgbNoise,(uv+ivec2(1,1))&255,0),f.x),f.y).yx;
    return -1.0+2.0*mix( rg.x, rg.y, f.z );
}

float fbm(vec3 p){
    float sum = 0.;
    float amp = 1.;
    float freq = 1.;
    for(int i = 0; i < 8; i++){
        sum += amp*noise(p*freq);
        amp *= .5;
        freq *= 2.;
//        p += vec3(7.123, 5.324, 121.2);
    }
    return sum;
}

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 color = vec3(fbm(vec3(0., uv)));
    gl_FragColor = vec4(color, 1.);
}
