package com.wyz.camera.core.gl.oes

import android.content.res.Resources
import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.wyz.camera.core.gl.BaseShader

class OesShader : BaseShader {
    companion object {
        val OES_VERT = """attribute vec4 aVertexCo;
                            attribute vec2 aTextureCo;
                            
                            uniform mat4 uVertexMatrix;
                            uniform mat4 uTextureMatrix;
                            
                            varying vec2 vTextureCo;
                            
                            void main(){
                                gl_Position = uVertexMatrix*aVertexCo;
                                vTextureCo = (uTextureMatrix*vec4(aTextureCo,0,1)).xy;
                            }"""
        val OES_FRAG = """#extension GL_OES_EGL_image_external : require
                                precision mediump float;
                                varying vec2 vTextureCo;
                                uniform samplerExternalOES uTexture;
                                void main() {
                                    gl_FragColor = texture2D( uTexture, vTextureCo);
                                }"""
    }

    constructor(resource: Resources) : super(resource, "shader/oes.vert", "shader/oes.frag")

    constructor() : super(null, OES_VERT, OES_FRAG)

    override fun onBindTexture(textureId: Int) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(mGLTexture, 0)
    }
}