
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

float sdBox( in vec2 p, in vec2 size ){
    vec2 d = abs(p)-size;
    return length(max(d,vec2(0))) + min(max(d.x,d.y),0.0);
}

vec3 render(vec2 uv){
    uv.y = 1.0 - uv.y;
    vec2 pos = vec2(0.5);
    vec2 size = vec2(.3);
    int count = 10;
    size.x += .03*sign(sin(uv.y*80.+time))*sin(uv.y*10.-time);
    size.y += .03*sign(sin(uv.x*80.+time))*sin(uv.x*10.-time);
    float box = sdBox(uv-pos, size);
    if(box < .0){
        uv.x = 1.-uv.x;
//        uv.y = 1. - uv.y;
    }
    vec3 col = texture2D(img, uv).rgb;
    return col;
}

vec3 aarender(vec2 uv){
    float off = (1./resolution.x)/4.;
    vec3 colA = render(uv+vec2(off, off));
    vec3 colB = render(uv+vec2(-off, off));
    vec3 colC = render(uv+vec2(off, -off));
    vec3 colD = render(uv+vec2(-off, -off));
    vec3 mixed = (colA+colB+colC+colD)/4.;
    return mixed;
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    gl_FragColor = vec4(aarender(uv), 1.);
}