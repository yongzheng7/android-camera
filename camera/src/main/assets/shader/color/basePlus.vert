attribute vec4 aVertexCo;
attribute vec4 aTextureCo;

uniform mat4 uVertexMatrix;
uniform mat4 uTextureMatrix;

uniform float uWidth;
uniform float uHeight;

varying vec2 vTextureCo;

varying vec2 leftTextureCoordinate;
varying vec2 rightTextureCoordinate;

varying vec2 topTextureCoordinate;
varying vec2 topLeftTextureCoordinate;
varying vec2 topRightTextureCoordinate;

varying vec2 bottomTextureCoordinate;
varying vec2 bottomLeftTextureCoordinate;
varying vec2 bottomRightTextureCoordinate;

void main()
{
     float tempWidth = uWidth * 1.0f ;
     float tempHeight = uHeight * 1.0f ;

     gl_Position = uVertexMatrix*aVertexCo;
     vTextureCo = aTextureCo.xy;

     vec2 widthStep = vec2(tempWidth, 0.0);
     vec2 heightStep = vec2(0.0, tempHeight);
     vec2 widthHeightStep = vec2(tempWidth, tempHeight);
     vec2 widthNegativeHeightStep = vec2(tempWidth, -tempHeight);

     leftTextureCoordinate = aTextureCo.xy - widthStep;
     rightTextureCoordinate = aTextureCo.xy + widthStep;

     topTextureCoordinate = aTextureCo.xy - heightStep;
     topLeftTextureCoordinate = aTextureCo.xy - widthHeightStep;
     topRightTextureCoordinate = aTextureCo.xy + widthNegativeHeightStep;

     bottomTextureCoordinate = aTextureCo.xy + heightStep;
     bottomLeftTextureCoordinate = aTextureCo.xy - widthNegativeHeightStep;
     bottomRightTextureCoordinate = aTextureCo.xy + widthHeightStep;
}