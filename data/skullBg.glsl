uniform vec2 resolution;
uniform float time;

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = gl_FragCoord.xy / resolution,xy;

    float a = atan(cv.y, cv.x);
    float d = length(cv);
    vec3 color = vec3(smoothstep(0.0, 0.05, d));

    color += smoothstep(1., 0., sin(d*500.+a-time*10.));



    gl_FragColor = vec4(color, 1.);
}
