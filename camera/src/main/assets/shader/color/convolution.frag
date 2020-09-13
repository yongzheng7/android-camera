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

const mediump mat3 convolutionMatrix = mat3(-1.0, 0.0, 1.0,-2.0, 0.0, 2.0,-1.0, 0.0, 1.0);

void main() {
    vec4 textureColor = texture2D(uTexture, vTextureCo);

    mediump vec4 bottomColor = texture2D(uTexture, bottomTextureCoordinate);
    mediump vec4 bottomLeftColor = texture2D(uTexture, bottomLeftTextureCoordinate);
    mediump vec4 bottomRightColor = texture2D(uTexture, bottomRightTextureCoordinate);
    mediump vec4 leftColor = texture2D(uTexture, leftTextureCoordinate);
    mediump vec4 rightColor = texture2D(uTexture, rightTextureCoordinate);
    mediump vec4 topColor = texture2D(uTexture, topTextureCoordinate);
    mediump vec4 topRightColor = texture2D(uTexture, topRightTextureCoordinate);
    mediump vec4 topLeftColor = texture2D(uTexture, topLeftTextureCoordinate);

    mediump vec4 resultColor = topLeftColor * convolutionMatrix[0][0] + topColor * convolutionMatrix[0][1] + topRightColor * convolutionMatrix[0][2];
             resultColor += leftColor * convolutionMatrix[1][0] + textureColor * convolutionMatrix[1][1] + rightColor * convolutionMatrix[1][2];
             resultColor += bottomLeftColor * convolutionMatrix[2][0] + bottomColor * convolutionMatrix[2][1] + bottomRightColor * convolutionMatrix[2][2];
    gl_FragColor = resultColor;
}