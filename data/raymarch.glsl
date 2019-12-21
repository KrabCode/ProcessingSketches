//TODO
// soft shadows https://www.shadertoy.com/view/Xds3zN
// cheap fbm https://www.shadertoy.com/view/XslGRr
// acos(-1) = pi

uniform vec2 resolution;
uniform vec3 translate;
uniform vec3 lightDirection;
uniform float diffuseMag;
uniform float specularMag;
uniform float distFOV;
uniform float rotate;
uniform float time;
uniform float shininess;

uniform int maxSteps = 100;
uniform float maxDist = 100.;
uniform float surfaceDist = .0001;

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


mediump vec4 permute(in mediump vec4 x){return mod(x*x*34.+x,289.);}
mediump float snoise(in mediump vec3 v){
    const mediump vec2 C = vec2(0.16666666666,0.33333333333);
    const mediump vec4 D = vec4(0,.5,1,2);
    mediump vec3 i  = floor(C.y*(v.x+v.y+v.z) + v);
    mediump vec3 x0 = C.x*(i.x+i.y+i.z) + (v - i);
    mediump vec3 g = step(x0.yzx, x0);
    mediump vec3 l = (1. - g).zxy;
    mediump vec3 i1 = min( g, l );
    mediump vec3 i2 = max( g, l );
    mediump vec3 x1 = x0 - i1 + C.x;
    mediump vec3 x2 = x0 - i2 + C.y;
    mediump vec3 x3 = x0 - D.yyy;
    i = mod(i,289.);
    mediump vec4 p = permute( permute( permute(
    i.z + vec4(0., i1.z, i2.z, 1.))
    + i.y + vec4(0., i1.y, i2.y, 1.))
    + i.x + vec4(0., i1.x, i2.x, 1.));
    mediump vec3 ns = .142857142857 * D.wyz - D.xzx;
    mediump vec4 j = -49. * floor(p * ns.z * ns.z) + p;
    mediump vec4 x_ = floor(j * ns.z);
    mediump vec4 x = x_ * ns.x + ns.yyyy;
    mediump vec4 y = floor(j - 7. * x_ ) * ns.x + ns.yyyy;
    mediump vec4 h = 1. - abs(x) - abs(y);
    mediump vec4 b0 = vec4( x.xy, y.xy );
    mediump vec4 b1 = vec4( x.zw, y.zw );
    mediump vec4 sh = -step(h, vec4(0));
    mediump vec4 a0 = b0.xzyw + (floor(b0)*2.+ 1.).xzyw*sh.xxyy;
    mediump vec4 a1 = b1.xzyw + (floor(b1)*2.+ 1.).xzyw*sh.zzww;
    mediump vec3 p0 = vec3(a0.xy,h.x);
    mediump vec3 p1 = vec3(a0.zw,h.y);
    mediump vec3 p2 = vec3(a1.xy,h.z);
    mediump vec3 p3 = vec3(a1.zw,h.w);
    mediump vec4 norm = inversesqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2, p2), dot(p3,p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;
    mediump vec4 m = max(.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.);
    return .5 + 12. * dot( m * m * m, vec4( dot(p0,x0), dot(p1,x1),dot(p2,x2), dot(p3,x3) ) );
}

float fbm (vec3 p) {
    float value = 0.0;
    float amplitude = 0.001;
    float frequency = 1.;
    for (int i = 0; i < 4; i++) {
        float n = snoise(p*frequency);
        value += amplitude * n;
        frequency *= 3.0;
        amplitude *= 0.5;
    }
    return value;
}

float sphere(vec3 p, float r){
    return length(p) - r;
}

dc getDistanceAndColor(vec3 p){
    float n = fbm(p);
    float s = sphere(p, 4.5);
    return dc(opSmoothIntersection(s, n, 2.), n*500.-time*.5, n*500);
}

ray raymarch(vec3 rayOrigin, vec3 dir){
    float distanceTraveled = 0.;
    dc distColor = dc(0., 0., 0.);
    vec3 p;
    float distClosest = maxDist;
    for (int i = 0; i < maxSteps; i++){
        p = rayOrigin+dir*distanceTraveled;
        distColor = getDistanceAndColor(p);
        distClosest = min(distClosest, distColor.d);
        if (distColor.d < surfaceDist || distanceTraveled > maxDist){
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
    col = step(r.distSum, maxDist)*col;
    gl_FragColor = vec4(col, 1);
}