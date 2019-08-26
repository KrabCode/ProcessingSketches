precision highp float;
precision highp int;

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif


uniform sampler2D palette;
uniform sampler2D texture;
uniform float t;
uniform float distortMag;
uniform vec2 resolution;
uniform vec2 offset;
uniform float iter;
uniform float scale;

uniform float hueStart;
uniform float hueRange;


vec3 rgb( in vec3 c ){
 vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0), 6.0)-3.0)-1.0, 0.0, 1.0 );
 rgb = rgb*rgb*(3.0-2.0*rgb);  return c.z * mix(vec3(1.0), rgb, c.y);
}

float mandelbrot(vec2 c){
    vec2 z = vec2(c.x+ + distortMag*sin(t), c.y + distortMag*cos(t));
	float i = 0.;
	while(i++ < iter){
        float x = (z.x * z.x - z.y * z.y) + c.x;
        float y = (z.y * z.x + z.x * z.y) + c.y;
        if(pow(x, 2.) + pow(y, 2.) > 4.){
            break;
        }
        z = vec2(x,y);
	}
    return i >= iter ? 0. : clamp(i / iter, 0., 1.);
//	return texture(palette, vec2(i == iter? 0.5 : i/100.), 0.).xyz;
}

void main() {
	vec2 uv = gl_FragCoord.xy / resolution;
	vec2 c = (gl_FragCoord.xy-.5*resolution.xy)/resolution.y;
    c *= scale;
    c += offset/resolution;
    float n = mandelbrot(c);
    gl_FragColor = vec4(rgb(vec3(hueStart+n*hueRange, 1.-n, clamp(n*1.2,0.,1.))), 1.);
}