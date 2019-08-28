    #version 130

    const vec3 LUMA = vec3(0.299, 0.587, 0.114);
    const float SPAN_MAX = 8.0;
    const float REDUCE_MUL = 1.0 / 8.0;
    const float REDUCE_MIN = 1.0 / 128.0;

    uniform sampler2D texture;
    uniform vec2 texOffset;

    in vec2 vertTexCoord;

    out vec4 fragColor;

    void main() {
        float lumaNW = dot(texture2D(texture, vertTexCoord.xy + vec2(-1.0, -1.0) * texOffset).rgb, LUMA);
        float lumaNE = dot(texture2D(texture, vertTexCoord.xy + vec2(+1.0, -1.0) * texOffset).rgb, LUMA);
        float lumaSW = dot(texture2D(texture, vertTexCoord.xy + vec2(-1.0, +1.0) * texOffset).rgb, LUMA);
        float lumaSE = dot(texture2D(texture, vertTexCoord.xy + vec2(+1.0, +1.0) * texOffset).rgb, LUMA);
        float lumaM  = dot(texture2D(texture, vertTexCoord.xy).rgb, LUMA);

        float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
        float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

        vec2 dir = vec2(-((lumaNW + lumaNE) - (lumaSW + lumaSE)), ((lumaNW + lumaSW) - (lumaNE + lumaSE)));

        float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * REDUCE_MUL), REDUCE_MIN);

        float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
        dir = min(vec2(SPAN_MAX, SPAN_MAX), max(vec2(-SPAN_MAX, -SPAN_MAX), dir * rcpDirMin)) * texOffset;

        vec3 rgbA =                      (1.0 / 2.0) * (texture2D(texture, vertTexCoord.xy + dir * (1.0 / 3.0 - 0.5)).rgb + texture2D(texture, vertTexCoord.xy + dir * (2.0 / 3.0 - 0.5)).rgb);
        vec3 rgbB = rgbA * (1.0 / 2.0) + (1.0 / 4.0) * (texture2D(texture, vertTexCoord.xy + dir * (0.0 / 3.0 - 0.5)).rgb + texture2D(texture, vertTexCoord.xy + dir * (3.0 / 3.0 - 0.5)).rgb);

        float lumaB = dot(rgbB, LUMA);
        if(lumaB < lumaMin || lumaB > lumaMax)
            fragColor = vec4(rgbA, 1.0);
        else
            fragColor = vec4(rgbB, 1.0);

    }
