uniform sampler2D texture;
uniform vec2 resolution;
uniform float dA;
uniform float dB;
uniform float feed;
uniform float kill;

#define pi  3.14159
#define tau 6.28318

vec2 offset(float x, float y){
    return vec2(x / resolution.x, y / resolution.y);
}

vec3 laplacianNeighborhoodColor(vec2 uv){
    float centerWeight = -1.;
    float adjacentWeight = .2;
    float diagonalWeight = .05;
    vec3 sum = vec3(0);
    sum += diagonalWeight * texture(texture, uv+offset(-1, 1)).rgb;
    sum += diagonalWeight * texture(texture, uv+offset(1, 1)).rgb;
    sum += diagonalWeight * texture(texture, uv+offset(-1, -1)).rgb;
    sum += diagonalWeight * texture(texture, uv+offset(1, -1)).rgb;
    sum += adjacentWeight * texture(texture, uv+offset(-1, 0)).rgb;
    sum += adjacentWeight * texture(texture, uv+offset(1, 0)).rgb;
    sum += adjacentWeight * texture(texture, uv+offset(0, 1)).rgb;
    sum += adjacentWeight * texture(texture, uv+offset(0, -1)).rgb;
    sum += centerWeight * texture(texture, uv).rgb;
    return sum;
}

void main(){
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 prev = texture(texture, uv).rgb;
    float a = prev.r;
    float b = prev.b;
    vec3 lap = laplacianNeighborhoodColor(uv);
    float la = lap.r;
    float lb = lap.b;
    float t = 1.;
    a += ((dA*la)-(a*b*b)+feed*(1-a))*t;
    b += ((dB*lb)+(a*b*b)-(kill+feed)*b)*t;
    vec3 col = vec3(a, 0., b);
    gl_FragColor = vec4(col, 1.);
}