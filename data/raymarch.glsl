precision highp float;
precision highp int;

uniform sampler2D texture;
uniform vec2 resolution;
uniform vec3 lightPos;
uniform vec3 origin;
uniform float time;
uniform float diffuse;
uniform float specular;
uniform float shininess;


const int MAX_STEPS = 500;
const float MAX_DISTANCE = 80.;
const float SURFACE_DISTANCE = 0.0001;

#define pi 3.14159265359
#define inf 9999.

//	Simplex 3D Noise
//	by Ian McEwan, Ashima Arts
//
vec4 permute(vec4 x){ return mod(((x*34.0)+1.0)*x, 289.0); }
vec4 taylorInvSqrt(vec4 r){ return 1.79284291400159 - 0.85373472095314 * r; }
float snoise(vec3 v){
    const vec2  C = vec2(1.0/6.0, 1.0/3.0);
    const vec4  D = vec4(0.0, 0.5, 1.0, 2.0);

    // First corner
    vec3 i  = floor(v + dot(v, C.yyy));
    vec3 x0 =   v - i + dot(i, C.xxx);

    // Other corners
    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);

    //  x0 = x0 - 0. + 0.0 * C
    vec3 x1 = x0 - i1 + 1.0 * C.xxx;
    vec3 x2 = x0 - i2 + 2.0 * C.xxx;
    vec3 x3 = x0 - 1. + 3.0 * C.xxx;

    // Permutations
    i = mod(i, 289.0);
    vec4 p = permute(permute(permute(
    i.z + vec4(0.0, i1.z, i2.z, 1.0))
    + i.y + vec4(0.0, i1.y, i2.y, 1.0))
    + i.x + vec4(0.0, i1.x, i2.x, 1.0));

    // Gradients
    // ( N*N points uniformly over a square, mapped onto an octahedron.)
    float n_ = 1.0/7.0;// N=7
    vec3  ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z *ns.z);//  mod(p,N*N)

    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);// mod(j,N)

    vec4 x = x_ *ns.x + ns.yyyy;
    vec4 y = y_ *ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);

    vec4 s0 = floor(b0)*2.0 + 1.0;
    vec4 s1 = floor(b1)*2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww;

    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);

    //Normalise gradients
    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    // Mix final noise value
    vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m*m, vec4(dot(p0, x0), dot(p1, x1),
    dot(p2, x2), dot(p3, x3)));
}

const int OCTAVES = 3;
float fbm(vec3 p){
    p.y *= 1.;
    p.y -= time*1.*p.y*.00001+time*5.;
    float freq = 0.1;
    float amp = 0.015;
    float sum = -0.0001;
    for (int i = 0; i < OCTAVES; i++){
        float n = snoise(p*freq);
        sum += n*amp;
        amp *= .5;
        freq *= 3.5;
        p += vec3(pi*1.2, pi*.2, pi*3.);
    }
    return sum;
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

vec3 repeat(vec3 p, vec3 c){
    return mod(p+0.5*c, c)-0.5*c;
}

float cube(vec3 p, vec3 b){
    vec3 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, max(q.y, q.z)), 0.0);
}

float maxcomp(vec2 p) {
    return max(p.x, p.y);
}

float sdCross(in vec3 p, float w){
    float da = maxcomp(abs(p.xy));
    float db = maxcomp(abs(p.yz));
    float dc = maxcomp(abs(p.zx));
    return min(da, min(db, dc))-w;
}

vec3 mengerSponge(vec3 p){
    float d = cube(p, vec3(1.0));
    vec3 res = vec3(d, 1.0, 0.0);
    float s = 1.0;
    for (int m=0; m<4; m++){
        vec3 a = mod(p*s, 2.0)-1.0;
        s *= 3.0;
        vec3 r = abs(1.0 - 3.0*abs(a));
        float da = max(r.x, r.y);
        float db = max(r.y, r.z);
        float dc = max(r.z, r.x);
        float c = (min(da, min(db, dc))-1.0)/s;
        if (c>d){
            d = c;
            res = vec3(d, 0.2*da*db*dc, (1.0+float(m))/4.0);
        }
    }
    return res;
}

float mandelbulb(vec3 p){
    float power = time*0.1;
    vec3 z = p;
    float dr = 1.;
    float r;
    for (int i = 0; i < 15; i++){
        r = length(z);
        if (r > 2){
            break;
        }
        float theta = acos(z.z / r) * power;
        float phi = atan(z.y, z.x) * power;
        float zr = pow(r, power);
        dr = pow(r, power-1) * power * dr + 1;
        z = zr * vec3(sin (theta) * cos(phi), sin(phi) * sin(theta), cos(theta));
        z += p;
    }
    return .5*log(r) * (r / dr);
}

float isosurface(vec3 p){
    float x = p.x;
    float y = p.y;
    float z = p.z;
    return sin(x)*sin(y)*sin(z)+sin(x)*cos(y)*cos(z)+cos(x)*sin(y)*cos(z)+cos(x)*cos(y)*sin(z)-0.07*(cos(4*x)+cos(4*y)+cos(4*z))+1.17;
}

float octahedron(vec3 p, float s){
    p = abs(p);
    return (p.x+p.y+p.z-s)*0.57735027;
}

float prism(vec3 p, vec2 h){
    vec3 q = abs(p);
    return max(q.z-h.y, max(q.x*0.866025+p.y*0.5, -p.y)-h.x*0.5);
}

float pyramid(vec3 p, float h){
    float m2 = h*h + 0.25;
    p.xz = abs(p.xz);
    p.xz = (p.z>p.x) ? p.zx : p.xz;
    p.xz -= 0.5;
    vec3 q = vec3(p.z, h*p.y - 0.5*p.x, h*p.x + 0.5*p.y);
    float s = max(-q.x, 0.0);
    float t = clamp((q.y-0.5*p.z)/(m2+0.25), 0.0, 1.0);
    float a = m2*(q.x+s)*(q.x+s) + q.y*q.y;
    float b = m2*(q.x+0.5*t)*(q.x+0.5*t) + (q.y-m2*t)*(q.y-m2*t);
    float d2 = min(q.y, -q.x*m2-q.y*0.5) > 0.0 ? 0.0 : min(a, b);
    float pyramid = sqrt((d2+q.z*q.z)/m2) * sign(max(q.z, -p.y));
    return pyramid;
}

float capsule(vec3 p, vec3 a, vec3 b, float radius){
    vec3 ab = b-a;
    vec3 ap = p-a;
    float t = dot(ab, ap) / dot(ab, ab);
    t = clamp(t, 0, 1);
    vec3 c = a + t*ab;
    float d = length(p-c)-radius;
    return d;
}

float sphere(vec3 p, vec3 pos, float r){
    vec4 sphere = vec4(pos.xyz, r);
    return length(p-sphere.xyz) - sphere.w;
}

float torus(vec3 p, vec2 r){
    float x = length(p.xz)-r.x;
    return length(vec2(x, p.y))-r.y;
}

float getDistance(vec3 p){
    float n = fbm(vec3(p.x, p.y, p.z));
    float shape = capsule(p, vec3(0, -1, 0), vec3(0, 10, 0), 2.);
    p.y += 3.;
    p.x *= 0.2;
    p.z *= 0.2;
    float pyramid = pyramid(p, 8.);
    shape = max(shape, pyramid);
    return max(n, shape);
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

vec4 rayMarch(vec3 rayOrigin, vec3 rayDirection){
    float distance = 0.;
    float maxDistance = MAX_DISTANCE;
    float lowestDistance = MAX_DISTANCE;
    vec3 p;
    for (int i = 0; i < MAX_STEPS; i++){
        p = rayOrigin+rayDirection*distance;
        float distanceToScene = getDistance(p);
        distance += distanceToScene;
        lowestDistance = min(distance, lowestDistance);
        if (distanceToScene < SURFACE_DISTANCE || distance > maxDistance){
            break;
        }
    }
    return vec4(p, max(distance, lowestDistance));
}

float getDiffuseLight(vec3 p, vec3 lightPos, vec3 lightDir, vec3 normal){
    float diffuseLight = max(dot(normal, -lightDir), 0.0);
    return diffuseLight;
}

float getSpecularLight(vec3 p, vec3 d, vec3 lightDir, vec3 normal) {
    vec3 reflectionDirection = reflect(-lightDir, normal);
    float specularAngle = max(dot(reflectionDirection, d), 0.0);
    return pow(specularAngle, shininess/4.0);
}


// read http://jamie-wong.com/2016/07/15/ray-marching-signed-distance-functions/
// study evvvvil on shadertoy
void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 rayOrigin = vec3(origin.x, -origin.y, origin.z);
    float t = time;
    rayOrigin.xz *= rotate2d(t);
    vec3 rayDirection = normalize(vec3(uv.x, uv.y, 1.));
    rayDirection.xz *= rotate2d(t);
    vec4 intersection = rayMarch(rayOrigin, rayDirection);
    vec3 normal = getNormal(intersection.xyz);
    vec3 lightOrigin = vec3(lightPos.xyz);
    lightOrigin.xz *= rotate2d(t);
    vec3 lightDir = normalize(intersection.xyz - lightOrigin);
    float diffuseLight = getDiffuseLight(intersection.xyz, lightOrigin, lightDir, normal);
    float specularLight = getSpecularLight(intersection.xyz, rayDirection, lightDir, normal);
    float pct = 0.1 + diffuseLight * diffuse + specularLight * specular;
    if (intersection.w > MAX_DISTANCE * .99){
        pct = 0;
    }
    gl_FragColor = vec4(rgb(
        .85-.02*intersection.y+0.3*pct,
        1.-pct*.3,
        smoothstep(0, 1., pct))
    , 1.0);
}