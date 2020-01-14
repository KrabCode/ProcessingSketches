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

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float d = length(cv);
    float pct = cubicPulse(0., 1., d);
    vec3 orig = texture2D(texture, uv).rgb;
    float hue = atan(cv.y, cv.x)+d-time*.5;
    float sat = 1.1;
    vec3 altered = rgb(vec3(.5+.4*abs(sin(hue)), sat, pct)).rgb;
    vec3 final = mix(orig, altered, alpha);
    gl_FragColor = vec4(final, 1.);
}