precision highp float;

uniform sampler2D texture;
uniform vec2 resolution;
uniform float shininess;
uniform float time;
uniform vec3 lightDir;

const vec3 specularColor = vec3(1);
uniform vec3 diffuseColor = vec3(.6, .8, .9);

const int octaves = 1;
const int steps = 1000;
const float surfaceDistance = 0.01;
const float normalDistance = 0.01;
const float maxDistance = 500;

#define pi 3.14159



struct raypath{
  vec3 hit;
  float closestDistance;
};

mat2 rotate2d(float angle){
    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
}

float sd(vec3 p){
    vec3 op = p;
    float radius = 3.;
    float sphere = length(p)-radius;
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
    float closestDistance = maxDistance;
    for(int i = 0; i < steps; i++){
        distanceToScene = sd(p);
        distanceTraveled += distanceToScene;
        closestDistance = min(closestDistance, distanceToScene);
        p = origin+direction*distanceTraveled;
        if(distanceToScene < surfaceDistance || distanceTraveled > maxDistance){
            break;
        }
    }
    return raypath(p, closestDistance);
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

vec3 render(vec2 cv){
    vec3 origin = vec3(0, 0.0,-10.);
    vec3 direction = normalize(vec3(cv, 1));
    raypath path = raymarch(origin, direction);
    vec3 color = vec3(0);
    if(length(path.hit) < maxDistance-10.){
        vec3 normal = getNormal(path.hit);
        vec3 lightDir = normalize(lightDir);
        float diffuse = getDiffuseLight(path.hit, lightDir, normal);
        float specular = getSpecularLight(lightDir, direction, normal);
        color = vec3(diffuse*diffuseColor + specular*specularColor);
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
