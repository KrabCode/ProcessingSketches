#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float alpha;
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;


float cubicPulse(float c, float w, float x){
    x = abs(x - c);
    if (x>w) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

void main(){
    vec2 ov = gl_FragCoord.xy/resolution.xy;
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
//        vec3 col = vec3(1);
    //    vec3 col = rgb(vec3(.58, .75, 1.));
    float t = time;
    vec3 col = 0.5 + 0.5*cos(t*0.2+uv.xyx+vec3(0, 2, 4));
    float d = length(uv)*1.5;
    float core = smoothstep(.8, 0.1, d);
    //    float triangle = cubicPulse(.0, 1.-pow(ov.y,0.5), uv.x);
    //    float triangle = cubicPulse(.0, .5, pow(abs(uv.y), .5));
    float ring = cubicPulse(.0, .1, .5+.5*sin(d*5.-t));
    col *= clamp(core+ring, 0., 1.);
    vec3 tex = texture(texture, ov).rgb;
    col = mix(tex, col, alpha);

    gl_FragColor = vec4(col, 1.);
}