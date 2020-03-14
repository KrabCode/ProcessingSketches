precision lowp float;

uniform sampler2D texture;

uniform vec2 resolution;
uniform float shininess;
uniform float time;
uniform vec3 lightDirection;

const vec3 glowColor = vec3(1, .6, .3);
const vec3 specularColor = glowColor;

const int steps = 300;
const float surfaceDistance = 0.001;
const float normalDistance = 0.001;
const float maxDistance = 100;

#define pi 3.14159
#define globalRotation -time*.1

float rand(float n){
    return fract(sin(n) * 43758.5453123);
}

float noise(float p){
    float fl = floor(p);
    float fc = fract(p);
    return mix(rand(fl), rand(fl + 1.0), fc);
}

float rand2D(in vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}
float rand3D(in vec3 co){
    return fract(sin(dot(co.xyz ,vec3(12.9898,78.233,144.7272))) * 43758.5453);
}

float dotNoise2D(in float x, in float y, in float fractionalMaxDotSize, in float dDensity)
{
    float integer_x = x - fract(x);
    float fractional_x = x - integer_x;

    float integer_y = y - fract(y);
    float fractional_y = y - integer_y;

    if (rand2D(vec2(integer_x+1.0, integer_y +1.0)) > dDensity)
    {return 0.0;}

    float xoffset = (rand2D(vec2(integer_x, integer_y)) -0.5);
    float yoffset = (rand2D(vec2(integer_x+1.0, integer_y)) - 0.5);
    float dotSize = 0.5 * fractionalMaxDotSize * max(0.25,rand2D(vec2(integer_x, integer_y+1.0)));

    vec2 truePos = vec2 (0.5 + xoffset * (1.0 - 2.0 * dotSize) , 0.5 + yoffset * (1.0 -2.0 * dotSize));

    float distance = length(truePos - vec2(fractional_x, fractional_y));

    return 1.0 - smoothstep (0.3 * dotSize, 1.0* dotSize, distance);

}

float map(float value, float start1, float stop1, float start2, float stop2){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

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
float fbm (vec4 p) {
    float sum = 0.;
    float amp = 1;
    float freq = 1;
    // Loop of octaves
    for (int i = 0; i < 4; i++) {
        sum += amp*snoise(p*freq);
        freq *= 2.;
        amp *= .5;
        p += vec4(3.123, 2.456, 1.121, 2.4545);
    }
    return sum;
}

mat2 rotate2d(float _angle){
    return mat2(cos(_angle),-sin(_angle),
    sin(_angle),cos(_angle));
}

struct raypath{
    vec3 hit;
    vec3 hitLocalCoord;
    int material;
    float distanceTraveled;
    float closestDistance;
    vec3 closestPoint;
    float glow;
};

struct distance{
    float dist;
    vec3 localCoord;
    float glow;
    int material; // 0 = planet, 1 = moon, 2 = ring
};

float opSubtraction( float d1, float d2 ) {
    return max(-d1,d2);
}

float sdTorus( vec3 p, vec2 t )
{
    vec2 q = vec2(length(p.xz)-t.x,p.y);
    return length(q)-t.y;
}

vec3 getColor(vec3 p, int material){
    return vec3(1);
}

float sdCappedCylinder( vec3 p, float radius, float height){
    vec2 d = abs(vec2(length(p.xz),p.y)) - vec2(radius,height);
    return min(max(d.x,d.y),0.0) + length(max(d,0.0));
}

distance sd(vec3 p){
    float dist = p.y;
    vec2 planeCoord = fract(p.xz);
    dist -= 1.0*sin(planeCoord.x+planeCoord.y+time);
    float glow = 0;
    int material = 0;
    return distance(dist, p, glow, material);
}

vec3 getNormal(vec3 p){
    float d = sd(p).dist;
    vec2 offset = vec2(normalDistance, 0.);
    float d1 = sd(p-offset.xyy).dist;
    float d2 = sd(p-offset.yxy).dist;
    float d3 = sd(p-offset.yyx).dist;
    vec3 normal = d - vec3(d1, d2, d3);
    return normalize(normal);
}

raypath raymarch(vec3 origin, vec3 direction){
    vec3 p = origin.xyz;
    float distanceTraveled = 0;
    distance d = sd(origin);
    float closestDistance = maxDistance;
    vec3 closestPoint = vec3(maxDistance);
    float glowSum = 0;
    for(int i = 0; i < steps; i++){
        d = sd(p);
        glowSum += d.glow;
        distanceTraveled += d.dist;
        if(d.dist < closestDistance){
            closestDistance = d.dist;
            closestPoint = p;
        }
        p = origin+direction*distanceTraveled;
        if(d.dist < surfaceDistance || distanceTraveled > maxDistance){
            break;
        }
    }
    return raypath(p, d.localCoord, d.material, distanceTraveled, closestDistance, closestPoint, glowSum);
}

float getDiffuseLight(vec3 p, vec3 lightDir, vec3 normal){
    float diffuseLight = max(dot(normal, -lightDir), 0.0);
    return diffuseLight;
}

float getSpecularLight(vec3 lightDir, vec3 rayDirection, vec3 normal) {
    vec3 lightNorm = normalize(-lightDir);
    vec3 halfVector = normalize(lightNorm - rayDirection);
    return pow(max(dot(normal, halfVector),0.), shininess);
}

float getShadow(vec3 p, vec3 lightDir, vec3 normal){
    vec3 shadowOrigin = vec3(p.xyz)+normal*0.01;
    vec3 shadowDirection = lightDir * -1;
    raypath path = raymarch(shadowOrigin, shadowDirection);
    if(path.distanceTraveled < maxDistance*.9){
        return 1;
    }
    return 0;
}

vec3 render(vec2 cv){
    vec3 origin = vec3(0., 2., 0.);
    vec3 rayDir = normalize(vec3(cv, 1));
    raypath path = raymarch(origin, rayDir);
    vec3 normal = getNormal(path.hit);
    vec3 lightDir = normalize(lightDirection);
    vec3 color = getColor(path.hitLocalCoord, path.material);
    float diffuse = getDiffuseLight(path.hit, lightDir, normal);
    float specular = getSpecularLight(path.hit, lightDir, normal);
    vec3 lit = diffuse*color + specular*specularColor;
    color = vec3(lit);
    if(path.distanceTraveled > maxDistance / 2.){
        // background was hit
        float freq = 50.;
        color = vec3(dotNoise2D(cv.x*freq, cv.y*freq, 0.3, 0.2));
    }
    return color;
}

vec3 aarender(vec2 cv){
    float off = (1./resolution.x)/4.;
    vec3 colA = render(cv+vec2(off, off));
    vec3 colB = render(cv+vec2(-off, off));
    vec3 colC = render(cv+vec2(off, -off));
    vec3 colD = render(cv+vec2(-off, -off));
    vec3 mixed = (colA+colB+colC+colD)/4.;
    return mixed;
}

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    gl_FragColor = vec4(aarender(cv), 1.);
}
