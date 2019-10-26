#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float time;

float cubicPulse(float c, float w, float x){
	x = abs(x - c);
	if (x>w) return 0.0;
	x /= w;
	return 1.0 - x*x*(3.0-2.0*x);
}

void main(){
	float t = time*.3;
	vec2 uv = gl_FragCoord.xy / resolution.xy;
	vec2 cv = (gl_FragCoord.xy-.5*resolution) / resolution.y;
	vec3 col = 0.5 + 0.5*cos(vec3(0,2,4));
	float aboveLine = smoothstep(-.1, 0.1, cv.y-.2*sin(cv.x*3.5+t));
	float line = clamp(cubicPulse(.2*sin(cv.x*3.5+t), .05, cv.y), 0., .5)*2.;
	col.rgb += .5*line;
	col -= aboveLine*.3;

	vec3 old = texture(texture, uv).rgb;

	gl_FragColor = vec4(col, 1.);
}