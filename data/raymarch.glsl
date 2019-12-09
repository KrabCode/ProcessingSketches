precision highp float;
precision highp int;

uniform sampler2D texture;
uniform vec2 resolution;
uniform vec3 lightPos;
uniform vec3 origin;
uniform float time;

const int MAX_STEPS = 1000;
const float MAX_DISTANCE = 1000.;
const float SURFACE_DISTANCE = 0.1;

#define pi 3.14159265359

vec4 permute(vec4 x){ return mod(((x*34.0)+1.0)*x, 289.0); }
float permute(float x){ return floor(mod(((x*34.0)+1.0)*x, 289.0)); }
vec4 taylorInvSqrt(vec4 r){ return 1.79284291400159 - 0.85373472095314 * r; }
float taylorInvSqrt(float r){ return 1.79284291400159 - 0.85373472095314 * r; }
vec4 grad4(float j, vec4 ip){
    const vec4 ones = vec4(1.0, 1.0, 1.0, -1.0);
    vec4 p, s;

    p.xyz = floor(fract (vec3(j) * ip.xyz) * 7.0) * ip.z - 1.0;
    p.w = 1.5 - dot(abs(p.xyz), ones.xyz);
    s = vec4(lessThan(p, vec4(0.0)));
    p.xyz = p.xyz + (s.xyz*2.0 - 1.0) * s.www;

    return p;
}
float snoise(vec4 v){
    const vec2  C = vec2(0.138196601125010504, // (5 - sqrt(5))/20  G4
    0.309016994374947451);// (sqrt(5) - 1)/4   F4
    // First corner
    vec4 i  = floor(v + dot(v, C.yyyy));
    vec4 x0 = v -   i + dot(i, C.xxxx);

    // Other corners

    // Rank sorting originally contributed by Bill Licea-Kane, AMD (formerly ATI)
    vec4 i0;

    vec3 isX = step(x0.yzw, x0.xxx);
    vec3 isYZ = step(x0.zww, x0.yyz);
    //  i0.x = dot( isX, vec3( 1.0 ) );
    i0.x = isX.x + isX.y + isX.z;
    i0.yzw = 1.0 - isX;

    //  i0.y += dot( isYZ.xy, vec2( 1.0 ) );
    i0.y += isYZ.x + isYZ.y;
    i0.zw += 1.0 - isYZ.xy;

    i0.z += isYZ.z;
    i0.w += 1.0 - isYZ.z;

    // i0 now contains the unique values 0,1,2,3 in each channel
    vec4 i3 = clamp(i0, 0.0, 1.0);
    vec4 i2 = clamp(i0-1.0, 0.0, 1.0);
    vec4 i1 = clamp(i0-2.0, 0.0, 1.0);

    //  x0 = x0 - 0.0 + 0.0 * C
    vec4 x1 = x0 - i1 + 1.0 * C.xxxx;
    vec4 x2 = x0 - i2 + 2.0 * C.xxxx;
    vec4 x3 = x0 - i3 + 3.0 * C.xxxx;
    vec4 x4 = x0 - 1.0 + 4.0 * C.xxxx;

    // Permutations
    i = mod(i, 289.0);
    float j0 = permute(permute(permute(permute(i.w) + i.z) + i.y) + i.x);
    vec4 j1 = permute(permute(permute(permute (
    i.w + vec4(i1.w, i2.w, i3.w, 1.0))
    + i.z + vec4(i1.z, i2.z, i3.z, 1.0))
    + i.y + vec4(i1.y, i2.y, i3.y, 1.0))
    + i.x + vec4(i1.x, i2.x, i3.x, 1.0));
    // Gradients
    // ( 7*7*6 points uniformly over a cube, mapped onto a 4-octahedron.)
    // 7*7*6 = 294, which is close to the ring size 17*17 = 289.

    vec4 ip = vec4(1.0/294.0, 1.0/49.0, 1.0/7.0, 0.0);

    vec4 p0 = grad4(j0, ip);
    vec4 p1 = grad4(j1.x, ip);
    vec4 p2 = grad4(j1.y, ip);
    vec4 p3 = grad4(j1.z, ip);
    vec4 p4 = grad4(j1.w, ip);

    // Normalise gradients
    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;
    p4 *= taylorInvSqrt(dot(p4, p4));

    // Mix contributions from the five corners
    vec3 m0 = max(0.6 - vec3(dot(x0, x0), dot(x1, x1), dot(x2, x2)), 0.0);
    vec2 m1 = max(0.6 - vec2(dot(x3, x3), dot(x4, x4)), 0.0);
    m0 = m0 * m0;
    m1 = m1 * m1;
    return 49.0 * (dot(m0*m0, vec3(dot(p0, x0), dot(p1, x1), dot(p2, x2)))
    + dot(m1*m1, vec2(dot(p3, x3), dot(p4, x4))));
}
const int OCTAVES = 3;
float fbm(float x, float y, float z, float w){
    vec4 v = vec4(x, y, z, w);
    float freq = 3.;
    float amp = 1.;
    float sum = 0.;
    for (int i = 0; i < OCTAVES; i++){
        float n = snoise(vec4(v.x*freq, v.y*freq, v.z*freq, v.w*freq));
        sum += n*amp;
        amp *= .5;
        freq *= 2.;
        v.xy += vec2(51.212, 12.312);
    }
    return sum;
}
float fbm(float x){
    return fbm(x, 0., 0., 0.);
}
float fbm(float x, float y){
    return fbm(x, y, 0., 0.);
}
float fbm(float x, float y, float z){
    return fbm(x, y, z, 0.);
}

vec3 rgb(float h, float s, float b){
    vec3 c = vec3(h, s, b);
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0, 4.0, 2.0), 6.0)-3.0)-1.0, 0.0, 1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);  return c.z * mix(vec3(1.0), rgb, c.y);
}
mat2 rotate2d(float angle){
    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
}
float angularDiameter(float r, float size) {
    return atan(2 * (size / (2 * r)));
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

float getIsosurface(vec3 p){
    float x = p.x;
    float y = p.y;
    float z = p.z;
    return sin(x)*sin(y)*sin(z)+sin(x)*cos(y)*cos(z)+cos(x)*sin(y)*cos(z)+cos(x)*cos(y)*sin(z)-0.07*(cos(4*x)+cos(4*y)+cos(4*z))+1.17;
}

float getCanyonDistance(vec3 p){
    float detail = 0.04;
    float canyonDistance = -.5 + p.y*3.5 + 2.*abs(fbm(
    p.x*detail,
    p.y*detail,
    p.z*detail+time*.15
    ))
    - abs(p.x)*abs(p.y);
    return canyonDistance;
}

float getCapsuleDistance(vec3 p, vec3 a, vec3 b, float radius){
    vec3 ab = b-a;
    vec3 ap = p-a;
    float t = dot(ab, ap) / dot(ab, ab);
    t = clamp(t, 0, 1);
    vec3 c = a + t*ab;
    float d = length(p-c)-radius;
    return d;
}

float getSphereDistance(vec3 p, vec3 pos, float r){
    vec4 sphere = vec4(pos.xyz, r);
    return length(p-sphere.xyz) - sphere.w;
}

float getTorusDistance(vec3 p, vec2 r){
    float x = length(p.xz)-r.x;
    return length(vec2(x, p.y))-r.y;
}

float getDistance(vec3 p){
    //    float sphereDistance = getSphereDistance(p, vec3(0., 3.5, 10.0), 1.5);
//    float capsuleDistance = getCapsuleDistance(p, vec3(-0.2, 2.5, 10.0), vec3(0.2, 4.5, 10.0), 1.);
//    float torusDistance = getTorusDistance(p-vec3(0, 2, 20), vec2(5.0, 0.5));
//    float plane = p.y;
    return getIsosurface(p);
}

float rayMarch(vec3 rayOrigin, vec3 rayDirection){
    float distance = 0.;
    float lowestDistance = MAX_DISTANCE;
    for (int i = 0; i < MAX_STEPS; i++){
        vec3 p = rayOrigin+rayDirection*distance;
        float distanceToScene = getDistance(p);
        distance += distanceToScene;
        if (distance < lowestDistance){
            lowestDistance = distance;
        }
        if (distance > MAX_DISTANCE || distanceToScene < SURFACE_DISTANCE){
            break;
        }
    }
    return max(distance, lowestDistance*1.);
}

vec3 getNormal(vec3 p){
    float d = getDistance(p);
    vec2 offset = vec2(0.01, 0.);
    vec3 normal = d - vec3(
    getDistance(p-offset.xyy),
    getDistance(p-offset.yxy),
    getDistance(p-offset.yyx)
    );
    return normalize(normal);
}

float getDiffuseLight(vec3 p){
    vec3 lightDir = normalize(lightPos-p);
    vec3 normal = getNormal(p);
    float diffuseLight = dot(normal, lightDir);
    float shadowRay = rayMarch(p+normal*SURFACE_DISTANCE, lightDir);
    if (shadowRay < length(lightPos-p)){
        diffuseLight *= 0.2;
    }
    return diffuseLight;
}

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 rayOrigin = vec3(origin.x, -origin.y, origin.z);
    float lookDown = 0.0;
    vec3 rayDirection = normalize(vec3(uv.x, uv.y-lookDown, 0.05));
    float d = rayMarch(rayOrigin, rayDirection);
    vec3 intersection = rayOrigin + rayDirection * d;
//    float diffuseLight = getDiffuseLight(intersection);
//    float pct = getDiffuseLight(intersection);
    float pct = d*.001*d;
    gl_FragColor = vec4(rgb(.5+pct*0.8, 1.-pct, pct), 1.);
}