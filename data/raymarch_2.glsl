uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

float sdPyramid( vec3 p, float h)
{
    float m2 = h*h + 0.25;

    p.xz = abs(p.xz);
    p.xz = (p.z>p.x) ? p.zx : p.xz;
    p.xz -= 0.5;

    vec3 q = vec3( p.z, h*p.y - 0.5*p.x, h*p.x + 0.5*p.y);

    float s = max(-q.x,0.0);
    float t = clamp( (q.y-0.5*p.z)/(m2+0.25), 0.0, 1.0 );

    float a = m2*(q.x+s)*(q.x+s) + q.y*q.y;
    float b = m2*(q.x+0.5*t)*(q.x+0.5*t) + (q.y-m2*t)*(q.y-m2*t);

    float d2 = min(q.y,-q.x*m2-q.y*0.5) > 0.0 ? 0.0 : min(a,b);

    return sqrt( (d2+q.z*q.z)/m2 ) * sign(max(q.z,-p.y));
}

float sdTorus( vec3 p, vec2 t ){
    vec2 q = vec2(length(p.xz)-t.x,p.y);
    return length(q)-t.y;
}

float sd(vec3 p){
    float sphere = length(p-vec3(0,0,1))-.5;
    float cutout = 0.2*sin(p.y*40.+time);
    return max(sphere, cutout);
}

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 dir = vec3(uv, 1.0);
    float stepSize = 0.02;
    int steps = 50;
    float closest = 1.;
    vec3 origin = vec3(uv, 0.);
    vec3 p = vec3(0);
    for(int i = 0; i < steps; i++){
        p = origin+dir*i*stepSize;
        closest = min(closest,sd(p));
    }
    vec3 color = vec3(smoothstep(0.1, 0.0, closest));
    gl_FragColor = vec4(color, 1.);
}