precision mediump float;
uniform sampler2D uTexture;

varying vec2 vTextureCo;

uniform float uTexWidth;
uniform float uTexHeight;

const highp float quantizationLevels = 10.0;
const highp float threshold = 0.2;

void main() {
    vec4 textureColor = texture2D(uTexture, vTextureCo);

     vec2 widthStep = vec2(uTexWidth, 0.0);
     vec2 heightStep = vec2(0.0, uTexHeight);
     vec2 widthHeightStep = vec2(uTexWidth, uTexHeight);
     vec2 widthNegativeHeightStep = vec2(uTexWidth, -uTexHeight);

    float bottomLeftIntensity = texture2D(uTexture, vTextureCo.xy - widthNegativeHeightStep).r;
    float topRightIntensity = texture2D(uTexture, vTextureCo.xy + widthNegativeHeightStep).r;
    float topLeftIntensity = texture2D(uTexture, vTextureCo.xy - widthHeightStep).r;
    float bottomRightIntensity = texture2D(uTexture, vTextureCo.xy + widthHeightStep).r;
    float leftIntensity = texture2D(uTexture,  vTextureCo.xy - widthStep).r;
    float rightIntensity = texture2D(uTexture, vTextureCo.xy + widthStep).r;
    float bottomIntensity = texture2D(uTexture, vTextureCo.xy + heightStep).r;
    float topIntensity = texture2D(uTexture, vTextureCo.xy - heightStep).r;

    float h = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity + bottomRightIntensity;
    float v = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;

    float mag = length(vec2(h, v));
    vec3 posterizedImageColor = floor((textureColor.rgb * quantizationLevels) + 0.5) / quantizationLevels;
    float thresholdTest = 1.0 - step(threshold, mag);
    gl_FragColor = vec4(posterizedImageColor * thresholdTest, textureColor.a);
}