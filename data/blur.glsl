#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// blur shader based on
// https://github.com/cansik/processing-postfx/blob/master/shader/blurFrag.glsl

uniform sampler2D texture;
uniform vec2 resolution;
uniform int blurSize;
uniform float sigma;

uniform bool horizontal;

const float pi = 3.14159265;

void main() {
	vec2 pos = gl_FragCoord.xy;
	vec2 uv = pos / resolution;

  float numBlurPixelsPerSide = float(blurSize / 2); 
 
  vec2 blurMultiplyVec = horizontal ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
 
  // Incremental Gaussian Coefficent Calculation (See GPU Gems 3 pp. 877 - 889)
  vec3 incrementalGaussian;
  incrementalGaussian.x = 1.0 / (sqrt(2.0 * pi) * sigma);
  incrementalGaussian.y = exp(-0.5 / (sigma * sigma));
  incrementalGaussian.z = incrementalGaussian.y * incrementalGaussian.y;
 
  vec4 avgValue = vec4(0.0, 0.0, 0.0, 0.0);
  float coefficientSum = 0.0;
 
  // Take the central sample first...
  avgValue += texture2D(texture, uv) * incrementalGaussian.x;
  coefficientSum += incrementalGaussian.x;
  incrementalGaussian.xy *= incrementalGaussian.yz;
 
  // Go through the remaining vertical samples (half on each side of the center)
  for (float i = 1.0; i <= numBlurPixelsPerSide; i++) { 
    avgValue += texture2D(texture, (pos - i * blurMultiplyVec) / resolution) * incrementalGaussian.x;
    avgValue += texture2D(texture, (pos + i * blurMultiplyVec) / resolution) * incrementalGaussian.x;
    coefficientSum += 2.0 * incrementalGaussian.x;
    incrementalGaussian.xy *= incrementalGaussian.yz;
  }
 
  gl_FragColor = avgValue / coefficientSum;
}