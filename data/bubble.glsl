#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float pct = texture(texture, uv).a;
    vec3 bubble = texture(texture, uv).rgb * pct;
    float border = step(bubble.x + bubble.y + bubble.z, 2.0)*(pct);
//    pct *= .1+cv.x+cv.y;
    pct *= 1.-1.5*distance(cv, vec2(.2, .2));
    pct = max(border, pct);
    pct = clamp(pct, 0, 1);
    gl_FragColor = vec4(vec3(1.), pct);
}