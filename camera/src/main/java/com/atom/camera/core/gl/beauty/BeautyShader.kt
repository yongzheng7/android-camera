package com.atom.camera.core.gl.beauty

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.camera.core.gl.BaseShader

class BeautyShader :BaseShader{

    private var mGLaaCoef = 0
    private var mGLmixCoef = 0
    private var mGLiternum = 0

    private val aaCoef = 0f
    private val mixCoef = 0f
    private val iternum = 0

    constructor(resource: Resources) : super(resource, "shader/beauty/beauty.vert", "shader/beauty/beauty.frag"){
        needUseExpandConfig(true)
        setBeautyLevel(0)
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()
        mGLaaCoef = GLES20.glGetUniformLocation(mGLProgram, "uACoef")
        mGLmixCoef = GLES20.glGetUniformLocation(mGLProgram, "uMixCoef")
        mGLiternum = GLES20.glGetUniformLocation(mGLProgram, "uIternum")
    }

    fun setBeautyLevel(level: Int): BeautyShader {
        when (level) {
            1 -> setBeautyLevel(1, 0.19f, 0.54f)
            2 -> setBeautyLevel(2, 0.29f, 0.54f)
            3 -> setBeautyLevel(3, 0.17f, 0.39f)
            4 -> setBeautyLevel(3, 0.25f, 0.54f)
            5 -> setBeautyLevel(4, 0.13f, 0.54f)
            6 -> setBeautyLevel(4, 0.19f, 0.69f)
            else -> setBeautyLevel(0, 0f, 0f)
        }
        return this
    }

    private fun setBeautyLevel(a: Int, b: Float, c: Float) {
        runOnGLThread(Runnable {
            GLES20.glUniform1f(mGLaaCoef, b)
            GLES20.glUniform1f(mGLmixCoef, c)
            GLES20.glUniform1i(mGLiternum, a)
        })
    }
}