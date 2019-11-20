precision highp float;

uniform vec2 resolution;
uniform sampler2D texture;
uniform float delta;
uniform float power;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec2 cc = (gl_FragCoord.xy-.5*resolution.xy) / resolution.y;
    float d = delta*pow(length(cc), power);
    float a = atan(cc.y, cc.x);
    vec2 offset = vec2(d*cos(a)/resolution.x, d*sin(a)/resolution.y);
    vec3 color = texture(texture, uv+offset).rgb;
    gl_FragColor = vec4(color,1.);
}
