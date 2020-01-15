uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform float alpha;

#define pi 3.14159265359

vec3 rgb(vec3 c ){
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0), 6.0)-3.0)-1.0, 0.0, 1.0 );
    rgb = rgb*rgb*(3.0-2.0*rgb);  return c.z * mix(vec3(1.0), rgb, c.y);
}

float cubicPulse( float c, float w, float x ){
    x = abs(x - c);
    if( x>w ) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

float ease(float p, float g) {
    if (p < 0.5){
        return 0.5f * pow(2 * p, g);
    }
    return 1 - 0.5f * pow(2 * (1 - p), g);
}

void main(){
    float t = time*0.5;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float d = length(cv);
    vec3 color = vec3(0.);
    cv *= 100.;
    vec2 gv = fract(cv)-.5;
    vec2 id = abs(floor(cv)+0.5);
    float idd = length(id);
    gv.x += .2*cos(t*idd);
    gv.y += .2*sin(t*idd);
    color += 1.-ease(length(gv)*2.5, 5.);
    vec3 old = texture2D(texture, uv).rgb;
    color = mix(old, color, alpha);
    gl_FragColor = vec4(color, alpha);
}