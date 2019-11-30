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
	vec2 value =  dir * delta;

	vec2 neighbour1 = vertTexCoord.xy - value / resolution.x;
	neighbour1 = clamp(neighbour1, 0, 1);
	vec2 neighbour2 = vertTexCoord.xy - value / resolution.x;
	neighbour2 = clamp(neighbour2, 0, 1);

	vec4 c1 = texture2D(texture, neighbour1);
	vec4 c2 = texture2D(texture, vertTexCoord.xy);
	vec4 c3 = texture2D(texture, neighbour2);

	gl_FragColor = vec4(
		max(max(c1.r, c2.r), c3.r),
		max(max(c1.g, c2.g), c3.g),
		max(max(c1.b, c2.b), c3.b),
		max(max(c1.a, c2.a), c3.a)
	);
}