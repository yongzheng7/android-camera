package com.wyz.common.core.gl.func

import android.content.res.Resources
import android.opengl.GLES20
import com.wyz.common.core.gl.BaseShader

class FluorescenceShader : BaseShader {
    private var mGLTexture2 = 0
    private var mGLBorderColor = 0
    private var mGLStep = 0
    private var mBlackFilter: BlackMagicShader
    private var mTempTexture = 0
    private val mBorderColor = floatArrayOf(0f, 1f, 1f, 1f)
    private var mStep = 1.0f

    private var isAdd = true

    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/effect/fluorescence.frag") {
        needUseExpandConfig(true)
        mBlackFilter = BlackMagicShader(resource)
    }

    override fun create() {
        super.create()
        mBlackFilter.create()
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()
        mGLTexture2 = GLES20.glGetUniformLocation(mGLProgram, "uTexture2")
        mGLBorderColor = GLES20.glGetUniformLocation(mGLProgram, "uBorderColor")
        mGLStep = GLES20.glGetUniformLocation(mGLProgram, "uStep")
    }

    override fun sizeChanged(width: Int, height: Int) {
        super.sizeChanged(width, height)
        mBlackFilter.sizeChanged(width, height)
    }

    override fun draw(texture: Int) {
        mTempTexture = mBlackFilter.drawToTexture(texture)
        super.draw(texture)
    }

    override fun onBindTexture(textureId: Int) {
        super.onBindTexture(textureId)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTempTexture)
        GLES20.glUniform1i(mGLTexture2, 1)
    }

    override fun onDrawExpandConfig() {
        super.onDrawExpandConfig()
        if (isAdd) {
            mStep += 0.08f
        } else {
            mStep -= 0.08f
        }
        if (mStep >= 1.0f) {
            isAdd = false
            mStep = 1.0f
        } else if (mStep <= 0.0f) {
            isAdd = true
            mStep = 0.0f
        }
        GLES20.glUniform4fv(mGLBorderColor, 1, mBorderColor, 0)
        GLES20.glUniform1f(mGLStep, mStep)
    }
}