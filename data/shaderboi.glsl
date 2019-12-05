#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define pi 3.1415

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

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
    float amplitude = 1;
    float frequency = 1;
    // Loop of octaves
    for (int i = 0; i < 4; i++) {
        float n = noise(vec3(st.x*frequency, st.y*frequency, st.z));
        value += amplitude * n;
        st.xy *= rotate2d(amplitude+frequency);
        //        st += pi;
        frequency *= 5.;
        amplitude *= .45;
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
    float t = time*6.;
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    uv *= 40.;
    vec2 gv = fract(uv)-.5;
    vec3 col = vec3(0);
    col += length(gv);
    float d = length(uv);
    col *= 1.+.2*sin(d*.5-t);
    col += .3*(1-2*fbm(d*2.5-t));
    col = smoothstep(0.0, 0.7, col);
    float c = length(col);
    vec3 final = rgb(.5+c*0.4, 1.-c*.25, pow(c, 1.5));
    final = clamp(final, 0, 1);
    final -= smoothstep(0.,25.,d);
    gl_FragColor = vec4(final, 1.);
}