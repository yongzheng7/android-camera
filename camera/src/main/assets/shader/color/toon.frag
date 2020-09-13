precision mediump float;
uniform sampler2D uTexture;

varying vec2 vTextureCo;

varying vec2 leftTextureCoordinate;
varying vec2 rightTextureCoordinate;

varying vec2 topTextureCoordinate;
varying vec2 topLeftTextureCoordinate;
varying vec2 topRightTextureCoordinate;

varying vec2 bottomTextureCoordinate;
varying vec2 bottomLeftTextureCoordinate;
varying vec2 bottomRightTextureCoordinate;

const highp float quantizationLevels = 10.0;
const highp float threshold = 0.2;

void main() {
    vec4 textureColor = texture2D(uTexture, vTextureCo);

    float bottomLeftIntensity = texture2D(uTexture, bottomLeftTextureCoordinate).r;
    float topRightIntensity = texture2D(uTexture, topRightTextureCoordinate).r;
    float topLeftIntensity = texture2D(uTexture, topLeftTextureCoordinate).r;
    float bottomRightIntensity = texture2D(uTexture, bottomRightTextureCoordinate).r;
    float leftIntensity = texture2D(uTexture, leftTextureCoordinate).r;
    float rightIntensity = texture2D(uTexture, rightTextureCoordinate).r;
    float bottomIntensity = texture2D(uTexture, bottomTextureCoordinate).r;
    float topIntensity = texture2D(uTexture, topTextureCoordinate).r;
    float h = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity + bottomRightIntensity;
    float v = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;

    float mag = length(vec2(h, v));
    vec3 posterizedImageColor = floor((textureColor.rgb * quantizationLevels) + 0.5) / quantizationLevels;
    float thresholdTest = 1.0 - step(threshold, mag);
    gl_FragColor = vec4(posterizedImageColor * thresholdTest, textureColor.a);
}