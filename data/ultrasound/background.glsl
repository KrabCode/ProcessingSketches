#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

vec3 rgb(in vec3 hsb){
    vec3 rgb = clamp(abs(mod(hsb.x*6.0+vec3(0.0, 4.0, 2.0),
    6.0)-3.0)-1.0, 0.0, 1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);
    return hsb.z * mix(vec3(1.0), rgb, hsb.y);
}

float cubicPulse(float c, float w, float x){
    x = abs(x - c);
    if (x>w) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

void main(){
    vec2 ov = gl_FragCoord.xy/resolution.xy;
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 col = rgb(vec3(.58, .75, 1.));
    float d = length(uv)*1.5;
    float core = smoothstep(0.6, .0, d);
	float triangle = cubicPulse(.0, 1.-pow(ov.y,.75), uv.x);
	col *= core*triangle;
    vec3 tex = texture(texture, ov).rgb;
    col = mix(tex, col, .1);
    gl_FragColor = vec4(col, 1.);
}