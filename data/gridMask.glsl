#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define pi 3.14159

uniform sampler2D maskTex;
uniform vec2 resolution;
uniform float time;

void main(){
    float t = time*.2;
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float mask = (texture2D(maskTex, uv).r + texture2D(maskTex, uv).g + texture2D(maskTex, uv).b) / 3.;
    mask = step(mask, 0.75);
    vec3 col = vec3(0.);

    if(mask > .5){
        uv.y -= t/(pi);
    }else{
        uv.y += t/(pi);
    }

    vec2 gv = fract(uv*10.);
    vec2 id = floor(uv*10.)*.1;
    id *= 10.;
    float grid = mod(id.x + id.y, 2.);
    col += grid;
    gl_FragColor = vec4(col, 1.);
}