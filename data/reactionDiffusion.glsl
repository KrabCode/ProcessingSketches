uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;
uniform float dA;
uniform float dB;
uniform float feed;
uniform float kill;

#define pi  3.14159
#define tau 6.28318

float mod289(float x){return x - floor(x * (1.0 / 289.0)) * 289.0;}
vec4 mod289(vec4 x){return x - floor(x * (1.0 / 289.0)) * 289.0;}
vec4 perm(vec4 x){return mod289(((x * 34.0) + 1.0) * x);}
float noise(vec3 p){
    vec3 a = floor(p);
    vec3 d = p - a;
    d = d * d * (3.0 - 2.0 * d);

    vec4 b = a.xxyy + vec4(0.0, 1.0, 0.0, 1.0);
    vec4 k1 = perm(b.xyxy);
    vec4 k2 = perm(k1.xyxy + b.zzww);

    vec4 c = k2 + a.zzzz;
    vec4 k3 = perm(c);
    vec4 k4 = perm(c + 1.0);

    vec4 o1 = fract(k3 * (1.0 / 41.0));
    vec4 o2 = fract(k4 * (1.0 / 41.0));

    vec4 o3 = o2 * d.z + o1 * (1.0 - d.z);
    vec2 o4 = o3.yw * d.x + o3.xz * (1.0 - d.x);

    return o4.y * d.y + o4.x * (1.0 - d.y);
}

float noise(vec2 xy, float z){
    return noise(vec3(xy.x, xy.y, z));
}


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
    vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
    float dist = sin(1.-length(cv*50.));
    float angle = atan(cv.y, cv.x);
    float mag = .1 * sin(dist);
    float r = mag/resolution.x;
    uv += vec2(r*cos(angle), r*sin(angle));
    vec3 prev = texture(texture, uv).rgb;
    float a = prev.r;
    float b = prev.b;
    vec3 lap = laplacianNeighborhoodColor(uv);
    float la = lap.r;
    float lb = lap.b;
    float t = 1.;
    float d = 1.-length(cv);
    float f = feed;
    float k = kill;
    a += ((dA*la)-(a*b*b)+f*(1-a))*t;
    b += ((dB*lb)+(a*b*b)-(k+f)*b)*t;
    vec3 col = vec3(a, 0., b);
    gl_FragColor = vec4(col, 1.);
}