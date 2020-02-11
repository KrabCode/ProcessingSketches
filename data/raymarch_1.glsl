
uniform vec2 resolution;
uniform float time;

precision highp float;

struct hit{
    vec3 p;
    float closestDistance;
};

float sd(vec3 p){
    return length(p-vec3(0, 0, 0.2))-0.1;
}

hit raymarch(vec3 origin, vec3 direction){
    float distance = 0.;
    float maxDistance = 1000.;
    float minDistance = .0001;
    float closestDistance = 1000.;
    vec3 p;
    for (int i = 0; i < 8; i++){
        p = origin+direction*distance;
        float distanceToScene = sd(p);
        distance += distanceToScene;
        closestDistance = min(distanceToScene, closestDistance);
        if (distance > maxDistance || distance < minDistance){
            break;
        }
    }
    return hit(p, closestDistance);
}

vec3 render(vec2 cv){
    vec3 origin = vec3(0, 0, -1);
    vec3 dir = normalize(vec3(cv.x, cv.y, 1.));
    hit h = raymarch(origin, dir);
    return vec3(1.-h.closestDistance);
}

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution.xy) / resolution.y;
    gl_FragColor = vec4(render(cv), 1.);
}