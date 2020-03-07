
uniform sampler2D texture;
uniform sampler2D img;
uniform vec2 resolution;
uniform float time;

#define pi 3.14159

float random(float x){
    return fract(sin(x*35.12145)*345.4861);
}

vec2 random2(float x){
    return vec2(random(x-323.151), random(x+412.121));
}

vec2 wrapAround(vec2 p){
    vec2 x = vec2(1);
    return mod(p,x) - x*0.5;
}


void main(){
    vec2 uv = gl_FragCoord.xy / resolution;
    vec2 cv = (gl_FragCoord.xy-.5*resolution.xy)/resolution.y;
    vec3 normtex = vec3(0);
    vec3 fliptex = vec3(1);
     normtex = texture2D(img, vec2(uv.x, 1.-uv.y)).rgb;
     fliptex = texture2D(img, uv).rgb;

    float dist = length(cv);
    float distSin = sin(dist*25.-time);

    float a = (atan(cv.y, cv.x)*0.6);
    float spiralDist = length(cv) + a;
    float spiralSin = sin(spiralDist*10.-time);

    distSin += spiralSin;

    vec3 color = vec3(0);
    float transition = 0.15;

    if(distSin > 0){
        color = mix(normtex, fliptex, smoothstep(0., transition, distSin));
    }else{
        color = mix(fliptex, normtex, smoothstep(transition, 0., distSin));
    }
    gl_FragColor = vec4(color, 1.);
}