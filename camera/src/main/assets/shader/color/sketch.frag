precision highp float;

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

void main() {

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

        float mag = 1.0 - length(vec2(h, v));
        gl_FragColor = vec4(vec3(mag), 1.0);

}