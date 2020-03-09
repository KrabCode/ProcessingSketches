#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define pi 3.14159

uniform sampler2D texture;
uniform sampler2D img;
uniform vec2 resolution;
uniform float time;

float map(float value, float start1, float stop1, float start2, float stop2){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    uv.y = 1.-uv.y;

    float a = atan(cv.y, cv.x);
    a += pi;
    a /= pi*2;
    a += +.5/2.;
    a = fract(a);

//    a = abs(0.95*sin(a*2.));
    float d = max(abs(cv.x), abs(cv.y))*2.;

    if(a < .25/2. || a > 1.-.25/2.){
        d = length(cv)*1.41;
    }

//    d = pow(d, 0.9)*1.8;
    vec3 col = texture(img, vec2(a,d)).rgb;
    if(d > 1.){
        col *= 0.;
    }


    // a values turn from 0 to 1 abruptly, we need to cross-fade the image to hide the seam a little
    float transition = .01;
    if( a < transition){
        vec3 other = texture(img, vec2(1.-a,d)).rgb;
        col = mix(col, other, .5-map(a, 0, transition, 0, 0.5));
    }
    if(a > 1.-transition){
        vec3 other = texture(img, vec2(a-1.+transition, d)).rgb;
        col = mix(col, other, map(a, 1.-transition, 1., 0, 0.5));
    }
    gl_FragColor = vec4(col, 1.);
}