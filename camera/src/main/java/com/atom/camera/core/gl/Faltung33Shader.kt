package com.atom.camera.core.gl

import android.content.res.Resources
import android.opengl.GLES20

class Faltung33Shader : BaseShader {

    companion object{
        val FILTER_SHARPEN = floatArrayOf(0f, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f)
        val FILTER_BORDER = floatArrayOf(0f, 1f, 0f, 1f, -4f, 1f, 0f, 1f, 0f)
        val FILTER_CAMEO = floatArrayOf(2f, 0f, 2f, 0f, 0f, 0f, 3f, 0f, -6f)
    }
    private var mFaltung: FloatArray
    private var mGLFaltung = 0

    constructor(res: Resources, faltung: FloatArray) :super(res, "shader/base.vert", "shader/func/faltung3x3.frag") {
        needUseExpandConfig(true)
        mFaltung = faltung
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()
        mGLFaltung = GLES20.glGetUniformLocation(mGLProgram, "uFaltung")
    }

    override fun onDrawExpandConfig() {
        super.onDrawExpandConfig()
        GLES20.glUniformMatrix3fv(mGLFaltung, 1, false, mFaltung, 0)
    }
}