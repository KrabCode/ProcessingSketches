precision highp float;

uniform sampler2D texture;
uniform vec2 resolution;
uniform float shininess;
uniform float time;
uniform vec3 lightDir;

const int octaves = 3;
const int steps = 1000;
const float surfaceDistance = 0.01;
const float normalDistance = 0.01;
const float maxDistance = 500;

#define pi 3.14159

// noise is from here:
// https://www.shadertoy.com/view/4dS3Wd by Morgan McGuire @morgan3d!

// Precision-adjusted variations of https://www.shadertoy.com/view/4djSRW
float hash(float p) { p = fract(p * 0.011); p *= p + 7.5; p *= p + p; return fract(p); }
float hash(vec2 p) {vec3 p3 = fract(vec3(p.xyx) * 0.13); p3 += dot(p3, p3.yzx + 3.333); return fract((p3.x + p3.y) * p3.z); }

float noise(float x) {
    float i = floor(x);
    float f = fract(x);
    float u = f * f * (3.0 - 2.0 * f);
    return mix(hash(i), hash(i + 1.0), u);
}


float noise(vec2 x) {
    vec2 i = floor(x);
    vec2 f = fract(x);

    // Four corners in 2D of a tile
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    // Simple 2D lerp using smoothstep envelope between the values.
    // return vec3(mix(mix(a, b, smoothstep(0.0, 1.0, f.x)),
    //			mix(c, d, smoothstep(0.0, 1.0, f.x)),
    //			smoothstep(0.0, 1.0, f.y)));

    // Same code, with the clamps in smoothstep and common subexpressions
    // optimized away.
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}


float noise(vec3 x) {
    const vec3 step = vec3(110, 241, 171);

    vec3 i = floor(x);
    vec3 f = fract(x);

    // For performance, compute the base input to a 1D hash from the integer part of the argument and the
    // incremental change to the 1D based on the 3D -> 1D wrapping
    float n = dot(i, step);

    vec3 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix( hash(n + dot(step, vec3(0, 0, 0))), hash(n + dot(step, vec3(1, 0, 0))), u.x),
    mix( hash(n + dot(step, vec3(0, 1, 0))), hash(n + dot(step, vec3(1, 1, 0))), u.x), u.y),
    mix(mix( hash(n + dot(step, vec3(0, 0, 1))), hash(n + dot(step, vec3(1, 0, 1))), u.x),
    mix( hash(n + dot(step, vec3(0, 1, 1))), hash(n + dot(step, vec3(1, 1, 1))), u.x), u.y), u.z);
}

float fbm(float x) {
    float v = 0.0;
    float a = 0.5;
    float shift = float(100);
    for (int i = 0; i < octaves; ++i) {
        v += a * noise(x);
        x = x * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

vec3 rgb( in vec3 c ){
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0), 6.0)-3.0)-1.0, 0.0, 1.0 );
    rgb = rgb*rgb*(3.0-2.0*rgb);  return c.z * mix(vec3(1.0), rgb, c.y);
}

float fbm(vec2 x) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100);
    // Rotate to reduce axial bias
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.50));
    for (int i = 0; i < octaves; ++i) {
        v += a * noise(x);
        x = rot * x * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}


float fbm(vec3 x) {
    float v = 0.0;
    float a = 0.5;
    vec3 shift = vec3(100);
    for (int i = 0; i < octaves; ++i) {
        v += a * noise(x);
        x = x * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

struct raypath{
  vec3 hit;
};

mat2 rotate2d(float angle){
    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
}

float sd(vec3 p){
    vec3 op = p;
    float radius = 3.;
    float sphere = length(p)-radius;
    if(sphere < radius*1.0){
        float x = p.x;
        float y = p.z;
        float z = p.y;
        float lat = atan(z,sqrt(x*x+y*y));
        float lon = atan(y,x);
        sphere = length(p)-radius+fbm(lat*50.+lon*50.);
    }
    return sphere;
}

vec3 getNormal(vec3 p){
    float d = sd(p);
    vec2 offset = vec2(normalDistance, 0.);
    float d1 = sd(p-offset.xyy);
    float d2 = sd(p-offset.yxy);
    float d3 = sd(p-offset.yyx);
    vec3 normal = d - vec3(d1, d2, d3);
    return normalize(normal);
}

raypath raymarch(vec3 origin, vec3 direction){
    vec3 p = origin.xyz;
    float distanceTraveled = 0;
    float distanceToScene = sd(origin);
    for(int i = 0; i < steps; i++){
        distanceToScene = sd(p);
        distanceTraveled += distanceToScene;
        p = origin+direction*distanceTraveled;
        if(distanceToScene < surfaceDistance || distanceTraveled > maxDistance){
            break;
        }
    }
    return raypath(p);
}

float getDiffuseLight(vec3 p, vec3 lightDir, vec3 normal){
    float diffuseLight = max(dot(normal, -lightDir), 0.0);
    return diffuseLight;
}

float getSpecularLight(vec3 p, vec3 lightDir, vec3 rayDirection, vec3 normal) {
    vec3 reflectionDirection = reflect(-lightDir, normal);
    float specularAngle = max(dot(reflectionDirection, rayDirection), 0.);
    return pow(specularAngle, shininess);
}

vec3 render(vec2 cv){
    vec3 origin = vec3(0, 0.0,-10.);
    vec3 direction = normalize(vec3(cv, 1));
    raypath path = raymarch(origin, direction);
    vec3 color = vec3(0);
    if(length(path.hit) < maxDistance-10.){
        vec3 normal = getNormal(path.hit);
        vec3 lightDir = normalize(lightDir);
        float diffuse = getDiffuseLight(path.hit, lightDir, normal);
        float specular = getSpecularLight(path.hit, lightDir, direction, normal);
        float lit = clamp(diffuse + specular, 0, 1);
        color = vec3(lit);
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
    gl_FragColor = vec4(render(cv), 1.);
}
