uniform sampler2D rgbNoise;
uniform vec2 resolution;
uniform float time;

// Courtesy of Iñigo Quilez.
float noise( in vec3 x ){
    vec3 p = floor(x);
    vec3 f = fract(x);
    f = f*f*(3.0-2.0*f);
    vec2 uv = (p.xy+vec2(37.0,17.0)*p.z) + f.xy;
    vec2 rg = textureLod(rgbNoise, (uv+ 0.5)/256.0, 0.0 ).yx;
    return mix( rg.x, rg.y, f.z );
}

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 color = vec3(noise(vec3(uv*80.+time*20., 0.)));
    gl_FragColor = vec4(color, 1.);
}
