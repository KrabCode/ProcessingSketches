uniform sampler2D texture;
uniform sampler2D img;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float dist = length(cv)*0.5;
    float t = time;
    float timeOffset = .0002*sin(dist*50.-t);
    float theta = atan(cv.x, cv.y)+t*timeOffset;
    vec2 tv = vec2(dist*cos(theta), dist*sin(theta))+.5;
    vec3 tex = texture2D(img, tv).rgb;
    vec3 color = 1.-tex;
    gl_FragColor = vec4(color, 1.);
}