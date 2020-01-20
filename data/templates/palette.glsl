uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

float map(float x, float a1, float a2, float b1, float b2){
    return b1 + (b2-b1) * (x-a1) / (a2-a1);
}

vec3 color(float n, bool gradient){
    vec3[] palette = vec3[](
    vec3(255, 15, 15)/255.,
    vec3(255, 77, 0) /255.,
    vec3(255, 141, 0)/255.,
    vec3(255, 180, 0)/255.,
    vec3(255, 206, 0)/255.
    );
    int colorCount = palette.length();
    float step = 1./colorCount;
    for(int i = 1; i < colorCount; i++){
        float max = (i)*step;
        vec3 prevColor = palette[i-1];
        vec3 thisColor = palette[i];
        if(n < max){
            if(gradient){
                return mix(prevColor, thisColor, map(n, max-step, max, 0., 1.));
            }else{
                return prevColor;
            }
        }
    }
    return palette[palette.length()-1];
}


void main(){
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float pct = length(cv);
    vec3 c = color(pct, false);
    gl_FragColor = vec4(c, 1.);
}