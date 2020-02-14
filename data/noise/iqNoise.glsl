uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

float hash( float n )
{
    return fract(sin(n)*43758.5453);
}

float noise( vec3 x ){
    // The noise function returns a value in the range -1.0f -> 1.0f
    vec3 p = floor(x);
    vec3 f = fract(x);
    f  = f*f*(3.0-2.0*f);
    float n = p.x + p.y*57.0 + 113.0*p.z;
    return mix(mix(mix( hash(n+0.0), hash(n+1.0),f.x),
    mix( hash(n+57.0), hash(n+58.0),f.x),f.y),
    mix(mix( hash(n+113.0), hash(n+114.0),f.x),
    mix( hash(n+170.0), hash(n+171.0),f.x),f.y),f.z);
}


float fbm(vec3 p){
    float sum = 0.;
    float freq = 1.;
    float amp = 0.5;
    for(int i = 0; i < 6; i++){
        sum += amp*(1-2*noise(p*freq));
        freq *= 2.0;
        amp *= .5;
    }
    return sum;
}

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float d = length(cv);
    cv *= 8.;
    float noise = fbm(vec3(cv, time*0.1));
    vec3 col = vec3(noise);
    gl_FragColor = vec4(col, 1.);
}