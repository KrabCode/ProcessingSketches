uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    uv *= 40.;
    vec2 id = floor(uv)+.5;
    float d = .5+.5*cos(length(id*50.)+time);
    vec3 col = vec3(d);
    gl_FragColor = vec4(col, 1.);
}