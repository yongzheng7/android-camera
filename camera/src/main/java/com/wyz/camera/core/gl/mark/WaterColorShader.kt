package com.wyz.camera.core.gl.mark

import android.content.res.Resources
import android.opengl.GLES20
import com.wyz.camera.core.gl.BaseShader
import java.nio.ByteBuffer

class WaterColorShader : BaseShader {

    private val mGLWidth = 0
    private val mGLHeight = 0
    private var mGLNoise = 0

    private var mNoiseTextureId = 0

    constructor(res: Resources) : super(res, "shader/base.vert", "shader/effect/water_color.frag"){
        needUseExpandConfig(true)
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()
        mGLNoise = GLES20.glGetUniformLocation(mGLProgram, "uNoiseTexture")
    }

    override fun onSizeChanged(width: Int, height: Int) {
        super.onSizeChanged(width, height)
        mNoiseTextureId = createNoiseTexture(width, height)
    }

    override fun onDrawExpandConfig() {
        super.onDrawExpandConfig()
        GLES20.glUniform1f(mGLWidth, mWidth.toFloat())
        GLES20.glUniform1f(mGLHeight, mHeight.toFloat())
    }

    override fun onBindTexture(textureId: Int) {
        super.onBindTexture(textureId)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mNoiseTextureId)
        GLES20.glUniform1i(mGLNoise, 1)
    }


    private fun createNoiseTexture(width: Int, height: Int): Int {
        val tempTexture = IntArray(1)
        GLES20.glGenTextures(1, tempTexture, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tempTexture[0])
        val length = width * height * 3
        val data = ByteArray(length)
        for (i in 0 until length) {
            data[i] = (Math.random() * 8 - 4).toByte()
        }
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, width, height, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(data))
        return tempTexture[0]
    }
}