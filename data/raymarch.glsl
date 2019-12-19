uniform vec2 resolution;
uniform vec3 translate;
uniform vec3 lightDirection;
uniform float diffuseMag;
uniform float specularMag;
uniform float distFOV;
uniform float rotate;
uniform float time;
uniform float shininess;

int MAX_STEPS = 100;
float MAX_DIST = 100.;
float SURFACE_DIST = .0001;

struct ray{
    vec3 hit;
    float hue;
    float sat;
    float distClosest;
    float distSum;
};

struct dc{
    float d;
    float hue;
    float sat;
};

float getDiffuseLight(vec3 p, vec3 lightDir, vec3 normal){
    float diffuseLight = max(dot(normal, -lightDir), 0.0);
    return diffuseLight;
}

float getSpecularLight(vec3 p, vec3 lightDir, vec3 rayDirection, vec3 normal) {
    vec3 reflectionDirection = reflect(-lightDir, normal);
    float specularAngle = max(dot(reflectionDirection, rayDirection), 0.);
    return pow(specularAngle, shininess);
}

vec3 rgb(in vec3 hsb){
    vec3 rgb = clamp(abs(mod(hsb.x*6.0+
    vec3(0.0, 4.0, 2.0), 6.0)-3.0)-1.0, 0.0, 1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);
    return hsb.z * mix(vec3(1.0), rgb, hsb.y);
}

float opSmoothUnion(float d1, float d2, float k) {
    float h = clamp(0.5 + 0.5*(d2-d1)/k, 0.0, 1.0);
    return mix(d2, d1, h) - k*h*(1.0-h); }

float opSmoothSubtraction(float d1, float d2, float k) {
    float h = clamp(0.5 - 0.5*(d2+d1)/k, 0.0, 1.0);
    return mix(d2, -d1, h) + k*h*(1.0-h); }

float opSmoothIntersection(float d1, float d2, float k) {
    float h = clamp(0.5 - 0.5*(d2-d1)/k, 0.0, 1.0);
    return mix(d2, d1, h) + k*h*(1.0-h); }

float opUnion(float d1, float d2) { return min(d1, d2); }

float opSubtraction(float d1, float d2) { return max(-d1, d2); }

float opIntersection(float d1, float d2) { return max(d1, d2); }


mat2 rotate2d(float angle){
    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
}

float mod289(float x){ return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 mod289(vec4 x){ return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 perm(vec4 x){ return mod289(((x * 34.0) + 1.0) * x); }

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

float fbm (vec3 p) {
    float value = 0.0;
    float amplitude = 0.08;
    float frequency = 10;
    for (int i = 0; i < 6; i++) {
        float n = noise(p*frequency);
        value += amplitude * n;
        frequency *= 3.0;
        amplitude *= .5;
    }
    return value;
}

float sphere(vec3 p, float r){
    return length(p) - r;
}

dc getDistanceAndColor(vec3 p){
    float s = sphere(p, 1.);
    return dc(s, .55, 1.);
}

ray raymarch(vec3 rayOrigin, vec3 dir){
    float distanceTraveled = 0.;
    dc distColor = dc(0., 0., 0.);
    vec3 p;
    float distClosest = MAX_DIST;
    for (int i = 0; i < MAX_STEPS; i++){
        p = rayOrigin+dir*distanceTraveled;
        distColor = getDistanceAndColor(p);
        distClosest = min(distClosest, distColor.d);
        if (distColor.d < SURFACE_DIST || distanceTraveled > MAX_DIST){
            break;
        }
        distanceTraveled += distColor.d;
    }
    return ray(p, distColor.hue, distColor.sat, distClosest, distanceTraveled);
}

vec3 getNormal(vec3 p){
    dc d0 = getDistanceAndColor(p);
    float d = d0.d;
    vec2 offset = vec2(0.001, 0.);
    dc d1 = getDistanceAndColor(p-offset.xyy);
    dc d2 = getDistanceAndColor(p-offset.yxy);
    dc d3 = getDistanceAndColor(p-offset.yyx);
    vec3 normal = d - vec3(d1.d, d2.d, d3.d);
    return normalize(normal);
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 rayOrigin = vec3(translate.xyz);
    rayOrigin.xz *= rotate2d(rotate);
    vec3 rayDirection = normalize(vec3(cv.xy, distFOV));
    rayDirection.xz *= rotate2d(rotate);
    ray r = raymarch(rayOrigin, rayDirection);
    vec3 normal = getNormal(r.hit);
    vec3 lightDir = normalize(lightDirection);
    lightDir.xz *= rotate2d(rotate);
    float diffuse = getDiffuseLight(r.hit, lightDir, normal);
    float specular = getSpecularLight(r.hit,lightDir,  rayDirection, normal);
    vec3 hsb = vec3(r.hue, r.sat, diffuse*diffuseMag + specular*specularMag);
    vec3 col = rgb(hsb);
    col = step(r.distSum, MAX_DIST)*col;
    gl_FragColor = vec4(col, 1);
}