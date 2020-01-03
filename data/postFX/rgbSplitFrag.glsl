#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;

varying vec4 vertColor;
varying vec4 vertTexCoord;

uniform vec2 resolution;
uniform float delta;

void main() {
	vec2 dir = vertTexCoord.xy - vec2(.5);
	float d = 0.8*length(dir);
	normalize(dir);
	vec2 value = d * dir * delta;

	vec4 c1 = texture2D(texture, vertTexCoord.xy - value / resolution.x);
	vec4 c2 = texture2D(texture, vertTexCoord.xy);
	vec4 c3 = texture2D(texture, vertTexCoord.xy + value / resolution.y);

	//remove extra green because I hate green
	c2.g = c2.g*smoothstep(0., 2.5, c1.a + c2.a + c3.b);
	//remove extra blue because I hate blue too
	c2.b = c2.b*smoothstep(0., 4.5, c1.a + c2.a + c3.b);

	gl_FragColor = vec4(c1.r, c2.g, c3.b, c1.a + c2.a + c3.b);
}