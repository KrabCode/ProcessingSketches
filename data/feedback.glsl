#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define pi  3.14159
#define tau 6.28318

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

float mod289(float x){return x - floor(x * (1.0 / 289.0)) * 289.0;}
vec4 mod289(vec4 x){return x - floor(x * (1.0 / 289.0)) * 289.0;}
vec4 perm(vec4 x){return mod289(((x * 34.0) + 1.0) * x);}
float noise(vec3 p){
    vec3 a = floor(p);
    vec3 d = p - a;
    d = d * d * (3.0 - 2.0 * d);

    vec4 b = a.xxyy + vec4(0.0, 1.0, 0.0, 1.0);
    vec4 k1 = perm(b.xyxy);
    vec4 k2 = perm(k1.xyxy + b.zzww);

    vec4 c = k2 + a.zzzz;
    vec4 k3 = perm(c);
    vec4 k4 = perm(c + 1.0);

    vec4 o1 = fract(k3 * (1.0 / 41.0));
    vec4 o2 = fract(k4 * (1.0 / 41.0));

    vec4 o3 = o2 * d.z + o1 * (1.0 - d.z);
    vec2 o4 = o3.yw * d.x + o3.xz * (1.0 - d.x);

    return o4.y * d.y + o4.x * (1.0 - d.y);
}

float fbm(float x, float y, float z){
    vec3 v = vec3(x, y, z);
    float freq = 0.5;
    float amp = 1.;
    float sum = 0.;
    for (int i = 0; i < 8; i++){
        float n = noise(vec3(v.x*freq, v.y*freq, v.z*freq));
        sum += n*amp;
        amp *= .5;
        freq *= 2.;
//        v.xy += vec2(51.212, 12.312);
    }
    return sum;
}

vec2 offset(float x, float y){
    return vec2(x / resolution.x, y / resolution.y);
}

float map(float x, float a1, float a2, float b1, float b2){
    return b1 + (b2-b1) * (x-a1) / (a2-a1);
}

vec3 move(vec2 uv, float mag, float angle){
    vec3 orig = texture(texture, uv).rgb;
    mag *= (orig.r+orig.g+orig.b)/3.;
    float texel = mag/resolution.x;
    vec2 off = vec2(texel*cos(angle), texel*sin(angle));
    return texture(texture, uv+off).rgb;
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float angle = pi*fbm(uv.x, uv.y, time*0.5);
    float d = length(cv);
    float mag = 3.*smoothstep(0.5, 0.7, 1.-d);
    vec3 col = move(uv, mag, angle);
    gl_FragColor = vec4(col, 1.);
}