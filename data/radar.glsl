#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

vec3 rgb(float h, float s, float b){
    vec3 c = vec3(h, s, b);
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0, 4.0, 2.0), 6.0)-3.0)-1.0, 0.0, 1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);  return c.z * mix(vec3(1.0), rgb, c.y);
}

mat2 rotate2d(float angle){
    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
}

float angularDiameter(float r, float size) {
    return atan(2 * (size / (2 * r)));
}

float cubicPulse(float c, float w, float x){
    x = abs(x - c);
    if (x>w) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

void main(){
    float t = time;
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 blue = rgb(.59, 1., 1.);
    vec3 col = vec3(0);
    float d = length(uv);
    col += blue * cubicPulse(0, 0.3, sin(d*60+0.5)) * step(d, .4);

    vec3 bb = texture2D(texture, gl_FragCoord.xy/resolution.xy).rgb;
    bb -= .01;
    col = mix(bb, col, .01);

    uv *= rotate2d(t);
    float a = atan(uv.x, uv.y);
    float angularSize = angularDiameter(d,.01);

    col = max(col, blue * cubicPulse(0, angularSize, a) * step(d, .4));


    gl_FragColor = vec4(col, 1.);
}