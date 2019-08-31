#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;
uniform vec2 resolution;
uniform float strength;
uniform float level;

float col(float x, float y) {
	vec2 uv = (gl_FragCoord.xy + vec2(x, y)) / resolution;
	return texture2D(texture, uv).r;
}

vec4 normToColor(in vec3 norm) {
	vec3 col = normalize(norm) * 0.5 + 0.5;
	return vec4(col, 1.0);
}

void main() {
	vec2 uv = gl_FragCoord.xy / resolution;
	vec3 sobel = vec3(1.0, 2.0, 1.0);
	mat3 vals = mat3(col(-1.0,-1.0), col( 0.0,-1.0), col( 1.0,-1.0),
									 col(-1.0, 0.0), col( 0.0, 0.0), col( 1.0, 0.0),
									 col(-1.0, 1.0), col( 0.0, 1.0), col( 1.0, 1.0));
	float Gy = dot(vals[0], sobel) + dot(vals[2], -sobel);
	vals = transpose(vals);
	float Gx = dot(vals[0], -sobel) + dot(vals[2], sobel);
	vec3 norm = vec3(Gx * strength, Gy * strength, 1.0 / level);
  gl_FragColor = normToColor(norm);
}