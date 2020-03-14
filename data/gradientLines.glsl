uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

#define pi 3.14159

float sdLine( in vec2 p, in vec2 a, in vec2 b )
{
    vec2 pa = p-a, ba = b-a;
    float h = clamp( dot(pa,ba)/dot(ba,ba), 0.0, 1.0 );
    return length( pa - ba*h );
}

float sdRoundedLine( in vec2 p, in vec2 a, in vec2 b, in float r)
{
    return sdLine(p, a, b) - r;
}

mat2 rotate2d(float angle){
    return mat2(cos(angle),-sin(angle), sin(angle),cos(angle));
}

vec2 constrain(vec2 p){
    p += .5;
    p = fract(p);
    p -= .5;
    return p*2.;
}

float ease(float p, float g) {
    if (p < 0.5) return 0.5f * pow(2 * p, g);
    return 1 - 0.5f * pow(2 * (1 - p), g);
}

vec3 render(vec2 uv){
    vec3 col = 0.5 + 0.5*cos(sin(time)+uv.xyx+vec3(0,2,4));
    for(int i = 0; i < 1000; i++){
        vec2 origin = vec2(2.*sin(i*1234), 2.*sin(i*1512));
        vec2 size = vec2(0.0, .25+.1*sin(i*3214.));
        vec2 speed = vec2(0., sin(i*1001.25)*time*0.08);
        int type = 0;
        float directionCount = 2.;
        int directionInt = int(floor(directionCount*(.5+.5*(sin(i*1124.))))); // [0, 8]
        float directionNorm = directionInt/directionCount;
        float direction = pi*2*(directionNorm);
        size *= rotate2d(direction);
        speed *= rotate2d(direction);
        origin += speed;
        origin = constrain(origin);
        float line = sdRoundedLine(uv, origin, origin+size, abs(.03+0.015*sin(i*1121.2)));
        if(line < 0.){
            vec3 lineCol = 0.5 + 0.5*cos(.5*sin(time)+directionNorm+uv.xyx+vec3(10,3,8));
            col = mix(col, lineCol, smoothstep(0., -0.005, line));
        }
    }
    return col;
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
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    gl_FragColor = vec4(render(uv), 1.);
}