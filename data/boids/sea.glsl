#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define pi 3.1415

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform vec2 camera;

vec3 rgb(float r, float g, float b){
    vec3 c = vec3(r, g, b);
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0, 4.0, 2.0), 6.0)-3.0)-1.0, 0.0, 1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);  return c.z * mix(vec3(1.0), rgb, c.y);
}

mat2 rotate2d(float angle){
    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
}



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


float fbm (float x, float y, float z) {
    vec3 st = vec3(x, y, z);
    float value = 0.0;
    float amplitude = 1.0;
    float frequency = 1.0;
    // Loop of octaves
    for (int i = 0; i < 10; i++) {
        float n = noise(vec3(st.x*frequency, st.y*frequency, st.z));
        value += amplitude * n;
        st.xy *= rotate2d(amplitude+frequency);
        st += pi*3.;
        frequency *= 3.;
        amplitude *= .58;
    }
    return value;
}

float fbm(float x, float y){
    return fbm(x, y, 0.);
}

float fbm(float x){
    return fbm(x, 0., 0.);
}

float cubicPulse(float c, float w, float x){
    x = abs(x - c);
    if (x>w) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

void main(){
    float t = time*0.5;
    vec2 ov = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = ov.xy;
    uv.x -= (camera.x/resolution.x);//*(16./9.);
    uv.y += (camera.y/resolution.y);
    vec2 distortUv = vec2(fbm(uv.y,uv.x), fbm(uv.x, uv.y));
    distortUv *= 20.;
    float water = fbm(distortUv.y+cos(t),distortUv.x+sin(t), t);
    vec3 col = mix(rgb(.6, 1., .2), vec3(1.), 1.-pow(water, 0.4));
    float land = fbm(uv.x, uv.y, t);
    gl_FragColor = vec4(col, 1.);
}