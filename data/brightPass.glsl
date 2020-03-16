
uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec4 color = texture(texture, uv);
    if(color.r > 0.4){
        color = vec4(0);
    }else{
        color = vec4(vec3(0), 1);
    }
    gl_FragColor = color;
}