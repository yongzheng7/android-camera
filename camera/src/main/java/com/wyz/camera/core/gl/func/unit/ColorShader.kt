package com.wyz.camera.core.gl.func.unit

import android.content.res.Resources
import android.opengl.GLES20
import com.wyz.camera.core.gl.BaseShader

class ColorShader : BaseShader {

    protected var mGLColor = 0
    protected var mColorData: FloatArray

    constructor(resource: Resources, color: FloatArray = floatArrayOf(1.0f , 0f , 0f)) : super(resource, "shader/base.vert", "shader/color/color.frag") {
        needUseExpandConfig(true)
        this.mColorData = color
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()
        mGLColor = GLES20.glGetUniformLocation(mGLProgram, "uColor")
    }

    override fun onDrawExpandConfig() {
        super.onDrawExpandConfig()
        GLES20.glUniform3fv(mGLColor, 1,  mColorData, 0)
    }
}