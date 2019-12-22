//TODO
// soft shadows https://www.shadertoy.com/view/Xds3zN
// cheap fbm https://www.shadertoy.com/view/XslGRr
// acos(-1) = pi

precision highp float;

uniform vec2 resolution;
uniform vec3 translate;
uniform vec3 lightDirection;
uniform float diffuseMag;
uniform float specularMag;
uniform float distFOV;
uniform float rotate;
uniform float time;
uniform float shininess;

uniform int maxSteps = 1000;
uniform float maxDist = 500.;
uniform float surfaceDist = 0.00001;

#define pi 3.14159265359

struct ray{
    vec3 hit;
    float hue;
    float sat;
    float distClosest;
    float distSum;
};

struct dist{
    float d;
    int maxRefractions;
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

float noise(vec2 P){
    //  https://github.com/BrianSharpe/Wombat/blob/master/Perlin2D.glsl

    // establish our grid cell and unit position
    vec2 Pi = floor(P);
    vec4 Pf_Pfmin1 = P.xyxy - vec4( Pi, Pi + 1.0 );

    // calculate the hash
    vec4 Pt = vec4( Pi.xy, Pi.xy + 1.0 );
    Pt = Pt - floor(Pt * ( 1.0 / 71.0 )) * 71.0;
    Pt += vec2( 26.0, 161.0 ).xyxy;
    Pt *= Pt;
    Pt = Pt.xzxz * Pt.yyww;
    vec4 hash_x = fract( Pt * ( 1.0 / 951.135664 ) );
    vec4 hash_y = fract( Pt * ( 1.0 / 642.949883 ) );

    // calculate the gradient results
    vec4 grad_x = hash_x - 0.49999;
    vec4 grad_y = hash_y - 0.49999;
    vec4 grad_results = inversesqrt( grad_x * grad_x + grad_y * grad_y ) * ( grad_x * Pf_Pfmin1.xzxz + grad_y * Pf_Pfmin1.yyww );

    // Classic Perlin Interpolation
    grad_results *= 1.4142135623730950488016887242097;  // scale things to a strict -1.0->1.0 range  *= 1.0/sqrt(0.5)
    vec2 blend = Pf_Pfmin1.xy * Pf_Pfmin1.xy * Pf_Pfmin1.xy * (Pf_Pfmin1.xy * (Pf_Pfmin1.xy * 6.0 - 15.0) + 10.0);
    vec4 blend2 = vec4( blend, vec2( 1.0 - blend ) );
    return dot( grad_results, blend2.zxzx * blend2.wwyy );
}

float fbm (vec2 p) {
    float value = 0.;
    float amplitude = 5;
    float frequency = 0.03;
    for (int i = 0; i < 6; i++) {
        float n = noise(p*frequency);
        value += amplitude * n;
        frequency *= 2.5;
        amplitude *= 0.35;
    }
    return value;
}

float octahedron(vec3 p, float s){
    p = abs(p);
    return (p.x+p.y+p.z-s)*0.57735027;
}

float sphere(vec3 p, float r){
    return length(p) - r;
}

float tubularCube(vec3 p, vec2 s){
    p = abs(p);
    return max(p.x - s.x, p.y - s.y);
}


float doubleHelix(vec3 p){
    float r = 2.;
    float frq = 0.15;
    float w = 0.5;
    float helixA = tubularCube(vec3(p.x+r*sin(p.z*frq), p.y+r*cos(p.z*frq), p.z), vec2(w));
    float helixB = tubularCube(vec3(p.x+r*sin(pi+p.z*frq), p.y+r*cos(pi+p.z*frq), p.z), vec2(w));
    return min(helixA, helixB);
}

vec3 repeat(vec3 p, vec3 c){
    return mod(p+0.5*c, c)-0.5*c;
}

dist getDistance(vec3 p){
    float s = sphere(vec3(p)-translate-vec3(0,0,10), 1.);
    int refract = 0;
    float d = p.y + fbm(p.xz);
    if(s < d){
        refract = 1;
    }
    return dist(min(d, s), refract);
}

vec3 getNormal(vec3 p){
    dist d0 = getDistance(p);
    float d = d0.d;
    vec2 offset = vec2(0.01, 0.);
    dist d1 = getDistance(p-offset.xyy);
    dist d2 = getDistance(p-offset.yxy);
    dist d3 = getDistance(p-offset.yyx);
    vec3 normal = d - vec3(d1.d, d2.d, d3.d);
    return normalize(normal);
}

ray raymarch(vec3 rayOrigin, vec3 dir){
    float distanceTraveled = 0.;
    dist d = dist(0., 0);
    vec3 p;
    float distClosest = maxDist;
    int refractions = 0;
    for (int i = 0; i < maxSteps; i++){
        p = rayOrigin+dir*distanceTraveled;
        d = getDistance(p);
        distClosest = min(distClosest, d.d);
        if(d.d < surfaceDist && d.maxRefractions == 0){
            break;
        }
        if(d.d < surfaceDist && refractions < d.maxRefractions){
            vec3 n = getNormal(p);
            dir = refract(normalize(dir), normalize(n), 0.5);
            rayOrigin = p+dir;
            refractions++;
        }
        if (distanceTraveled > maxDist){
            break;
        }
        distanceTraveled += d.d;
    }
    return ray(p, 0, 0, distClosest, distanceTraveled);
}

vec3 render(vec2 cv){
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
    return col;
}

vec3 antiAliasRender(vec2 cv){
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
    gl_FragColor = vec4(antiAliasRender(cv), 1.);
}