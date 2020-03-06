uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform int colorCount;
uniform vec4 hsba_0;
uniform vec4 hsba_1;
uniform vec4 hsba_2;
uniform vec4 hsba_3;
uniform vec4 hsba_4;
uniform vec4 hsba_5;
uniform vec4 hsba_6;
uniform vec4 hsba_7;
uniform vec4 hsba_8;
uniform vec4 hsba_9;

#define pi 3.14159

vec3 rgb(in vec3 hsb){
    vec3 rgb = clamp(abs(mod(hsb.x*6.0+
    vec3(0.0, 4.0, 2.0), 6.0)-3.0)-1.0, 0.0, 1.0);
    rgb = rgb*rgb*(3.0-2.0*rgb);
    return hsb.z * mix(vec3(1.0), rgb, hsb.y);
}

float map(float value, float start1, float stop1, float start2, float stop2){
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

float cubicPulse( float c, float w, float x ){
    x = abs(x - c);
    if( x>w ) return 0.0;
    x /= w;
    return 1.0 - x*x*(3.0-2.0*x);
}

float fadeInAndOut(float c, float w, float x, float tran){
    return smoothstep(c-w-tran, c-w, x) * smoothstep(c+w+tran,c+w, x);
}

void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution.xy) / resolution.y;
    vec4 col = vec4(vec3(.0),1);
    float theta = atan(cv.y, cv.x);
    float d = length(cv);
    float t = time*0.5;

    float scaledown = 1000;

    float circle = fadeInAndOut(.15, .05, d, .01);
    if(circle > 0){
        float hue = (theta / (pi*2)) + t;
        hue = floor(hue*scaledown)/scaledown;
        col.rgb = rgb(vec3(hue, 1., circle));
    }

    float verLine = fadeInAndOut(.3, .03, abs(cv.x), .01);

    float hue = cv.y*.5+t*sign(cv.x);
    hue = floor(hue*scaledown)/scaledown;
    col.rgb += rgb(vec3(hue, 1., verLine));

    float horLine = fadeInAndOut(.3, .03, abs(cv.y), .01);
    if(horLine > 0 &&
        (((cv.y > 0 && cv.x < 0 && verLine == 0) || (cv.y > 0 && cv.x > 0)) || // under top left, over top right
        ((cv.y < 0 && cv.x < 0) || (cv.y < 0 && cv.x > 0  && verLine == 0)))   // over bot left, under bot right
    ){
        float hue = .5+cv.x*.5-t*sign(cv.y);
        hue = floor(hue*scaledown)/scaledown;
        col.rgb = rgb(vec3(hue, 1., horLine));
    }

    col.rgb = vec3(pow(col.r, .44), pow(col.g, .44), pow(col.b, .44));
    gl_FragColor = col;
}