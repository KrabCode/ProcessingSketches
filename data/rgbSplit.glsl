#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float easing;
uniform float strength;

float ease(float p, float g) {
	if (p < 0.5){
		return 0.5f * pow(2 * p, g);
	}else{
		return 1 - 0.5f * pow(2 * (1 - p), g);
	}
}

void main() {
	vec2 uv = gl_FragCoord.xy / resolution;
	vec2 cv = (gl_FragCoord.xy-.5*resolution.xy)/resolution.y;
	vec2 pixel = vec2(1./resolution.x, 1./resolution.y);
	float a = atan(cv.y, cv.x);
	float d = distance(cv, vec2(0.))*2.;
	vec2 off = vec2(ease(d,easing)*strength*cos(a), ease(d,easing)*strength*sin(a));
	float r = texture2D(texture, uv-off).r;
	float g = texture2D(texture, uv).g;
	float b = texture2D(texture, uv+off).b;
	vec3 rgb = vec3(r,g,b);
    gl_FragColor = vec4(rgb, 1.);
}