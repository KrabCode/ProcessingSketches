#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

float cubicPulse( float c, float w, float x ){
    x = abs(x - c);
    if( x>w ) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec3 col = 0.5 + 0.5*cos(time+uv.xyx+vec3(0,2,4));
    float r = .15;
    float d = length(uv);
    float w = .03;

    vec3 glyph = vec3(0.);

    float centralRing = cubicPulse(r, w, d);
    glyph += centralRing;

    uv.y -= r*2;
    d = length(uv);
    float upperArc = cubicPulse(r, w, d) * smoothstep(r*.1, 0., uv.y);
    glyph = max(glyph, upperArc);

    uv.y += r*4;
    float verticalLine = cubicPulse(uv.x, w, 0.) * smoothstep(r, r*.95, uv.y) * smoothstep(-r*.7, -r*.5, uv.y);
    float horizontalLine = cubicPulse(uv.y, w, 0.) * smoothstep(-r*.7, -r*.5, uv.x) * smoothstep(-r*.7, -r*.5, -uv.x);
    float cross = max(verticalLine, horizontalLine);
    glyph = max(glyph, cross);

    col *= glyph;

    col *= smoothstep(0., .5, col);

    gl_FragColor = vec4(col, 1.);
}