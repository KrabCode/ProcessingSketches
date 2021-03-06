#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform vec2 camera;
uniform float time;

#define pi 3.14159265359

vec3 rgb(float h, float s, float b){
    vec3 c = vec3(h, s, b);
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0, 4.0, 2.0), 6.0)-3.0)-1.0, 0.0, 1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);  return c.z * mix(vec3(1.0), rgb, c.y);
}

mat2 rotate2d(float angle){
    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
}

float angularDiameter(float r, float size) {
    return atan(2. * (size / (2. * r)));
}

float cubicPulse(float c, float w, float x){
    x = abs(x - c);
    if (x>w) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

float map(float value, float start1, float stop1, float start2, float stop2){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

float norm(float value, float start, float stop){
    return map(value, start, stop, 0., 1.);
}

//	Simplex 4D Noise
//	by Ian McEwan, Ashima Arts
//
vec4 permute(vec4 x){return mod(((x*34.0)+1.0)*x, 289.0);}
float permute(float x){return floor(mod(((x*34.0)+1.0)*x, 289.0));}
vec4 taylorInvSqrt(vec4 r){return 1.79284291400159 - 0.85373472095314 * r;}
float taylorInvSqrt(float r){return 1.79284291400159 - 0.85373472095314 * r;}
vec4 grad4(float j, vec4 ip){
    const vec4 ones = vec4(1.0, 1.0, 1.0, -1.0);
    vec4 p,s;
    p.xyz = floor( fract (vec3(j) * ip.xyz) * 7.0) * ip.z - 1.0;
    p.w = 1.5 - dot(abs(p.xyz), ones.xyz);
    s = vec4(lessThan(p, vec4(0.0)));
    p.xyz = p.xyz + (s.xyz*2.0 - 1.0) * s.www;
    return p;
}

float snoise(vec4 v){
    const vec2  C = vec2( 0.138196601125010504,  // (5 - sqrt(5))/20  G4
    0.309016994374947451); // (sqrt(5) - 1)/4   F4
    vec4 i  = floor(v + dot(v, C.yyyy) );
    vec4 x0 = v -   i + dot(i, C.xxxx);
    vec4 i0;
    vec3 isX = step( x0.yzw, x0.xxx );
    vec3 isYZ = step( x0.zww, x0.yyz );
     i0.x = isX.x + isX.y + isX.z;
    i0.yzw = 1.0 - isX;
    i0.y += isYZ.x + isYZ.y;
    i0.zw += 1.0 - isYZ.xy;
    i0.z += isYZ.z;
    i0.w += 1.0 - isYZ.z;
    vec4 i3 = clamp( i0, 0.0, 1.0 );
    vec4 i2 = clamp( i0-1.0, 0.0, 1.0 );
    vec4 i1 = clamp( i0-2.0, 0.0, 1.0 );
    vec4 x1 = x0 - i1 + 1.0 * C.xxxx;
    vec4 x2 = x0 - i2 + 2.0 * C.xxxx;
    vec4 x3 = x0 - i3 + 3.0 * C.xxxx;
    vec4 x4 = x0 - 1.0 + 4.0 * C.xxxx;
    i = mod(i, 289.0);
    float j0 = permute( permute( permute( permute(i.w) + i.z) + i.y) + i.x);
    vec4 j1 = permute( permute( permute( permute (
    i.w + vec4(i1.w, i2.w, i3.w, 1.0 ))
    + i.z + vec4(i1.z, i2.z, i3.z, 1.0 ))
    + i.y + vec4(i1.y, i2.y, i3.y, 1.0 ))
    + i.x + vec4(i1.x, i2.x, i3.x, 1.0 ));
    vec4 ip = vec4(1.0/294.0, 1.0/49.0, 1.0/7.0, 0.0) ;
    vec4 p0 = grad4(j0,   ip);
    vec4 p1 = grad4(j1.x, ip);
    vec4 p2 = grad4(j1.y, ip);
    vec4 p3 = grad4(j1.z, ip);
    vec4 p4 = grad4(j1.w, ip);
    vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2, p2), dot(p3,p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;
    p4 *= taylorInvSqrt(dot(p4,p4));
    vec3 m0 = max(0.6 - vec3(dot(x0,x0), dot(x1,x1), dot(x2,x2)), 0.0);
    vec2 m1 = max(0.6 - vec2(dot(x3,x3), dot(x4,x4)            ), 0.0);
    m0 = m0 * m0;
    m1 = m1 * m1;
    return 49.0 * ( dot(m0*m0, vec3( dot( p0, x0 ), dot( p1, x1 ), dot( p2, x2 )))
    + dot(m1*m1, vec2( dot( p3, x3 ), dot( p4, x4 ) ) ) ) ;
}

float fbm(vec4 p){
    float amp = 1.;
    float frq = 1.;
    float sum = 0.;
    for (int i = 0; i < 6; i++){
        sum += amp*snoise(p*frq);
        amp *= .3;
        frq *= 2.;
    }
    return sum;
}

float fbm(float x){
    return fbm(vec4(x, 0., 0., 0.));
}

float fbm(float x, float y){
    return fbm(vec4(x, y, 0., 0.));
}

float fbm(float x, float y, float z){
    return fbm(vec4(x, y, z, 0.));
}

float fbm(float x, float y, float z, float w){
    return fbm(vec4(x, y, z, w));
}

void main(){
    float t = time;
    float timeRadius = 0.035;
    vec2 timeOrigin = vec2(3, 4);
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 originalUV = uv.xy;
    uv.x -=  camera.x/resolution.x;
    uv.y +=  camera.y/resolution.y;
    uv = fract(uv*5.)-.5;
    uv *= 20.;
    vec2 gv = fract(uv)-.5;
    vec2 id = floor(uv)+.5;
    float distanceFromTileCenter = length(id*10.);
    float distanceFromOriginalCenter = length(originalUV*0.4);
    float rawNoise = fbm(
        distanceFromTileCenter, distanceFromOriginalCenter,
        timeOrigin.x+timeRadius*cos(t), timeOrigin.y+timeRadius*sin(t)
    );
    float smoothNoise = smoothstep(-0.5,1.5,rawNoise);
    float fadeToBlack = smoothstep(0.0, 1.5, length(originalUV));
    float hueStart = 0.57;
    float hueRange = 0.75;
    vec3 color = rgb(
        hueStart+hueRange*smoothNoise,
        1-smoothNoise,
        pow(rawNoise, 1.0)
    );
    color -= .25;
    gl_FragColor = vec4(color-fadeToBlack, 1.);
}



