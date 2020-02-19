precision lowp float;

uniform sampler2D texture;
uniform sampler2D noise;

uniform vec2 resolution;
uniform float shininess;
uniform float time;
uniform vec3 lightDir;

const vec3 specularColor = vec3(1);
const vec3 diffuseColor = vec3(.1, .2, 0.8);
const vec3 glowColor = vec3(1);

const int steps = 1000;
const float surfaceDistance = 0.0001;
const float normalDistance = 0.001;
const float maxDistance = 1000;

#define pi 3.14159

struct raypath{
    vec3 hit;
    float distanceTraveled;
    float closestDistance;
    vec3 closestPoint;
};

float fbm(vec3 p){
    float sum = 0.;
    float amp = 1.0;
    float freq = 0.8;
    for(int i = 0; i < 4.; i++){
        sum += amp*(sin(p.y*freq+time));
        freq *= 2.0;
        amp *= .5;
    }
    return sum;
}

mat2 rotate2d(float angle){
    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
}

float sd(vec3 p){
    float height = .1;
    if(p.y < height && p.y > -height){
        return p.y+height*fbm(vec3(p.xz, time));
    }
    return p.y;
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
    vec3 closestPoint = vec3(maxDistance);
    for(int i = 0; i < steps; i++){
        distanceToScene = sd(p);
        distanceTraveled += distanceToScene;
        if(distanceToScene < closestDistance){
            closestDistance = distanceToScene;
            closestPoint = p;
        }
        p = origin+direction*distanceTraveled;
        if(distanceToScene < surfaceDistance || distanceTraveled > maxDistance){
            break;
        }
    }
    return raypath(p, distanceTraveled, closestDistance, closestPoint);
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
    vec3 origin = vec3(0., 1., 0.);
    vec3 direction = normalize(vec3(cv, 1));
    raypath path = raymarch(origin, direction);
    vec3 color;
    vec3 normal = getNormal(path.hit);
    vec3 lightDir = normalize(lightDir);
    float diffuse = getDiffuseLight(path.hit, lightDir, normal);
    float specular = getSpecularLight(lightDir, direction, normal);
    vec3 lit = diffuse*diffuseColor + specular*specularColor;
    color = vec3(lit);
    if(path.distanceTraveled > maxDistance / 2.){
        color *= 0.;
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
