package com.wyz.camera.core.gl.func.unit

import android.content.res.Resources
import android.opengl.GLES20
import com.wyz.camera.core.gl.BaseShader

/**
 * 饱和度矩阵
 */
class SaturationShader : BaseShader {

    private var saturationType = 0
    private val saturationCode = 2.5f

    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/saturation.frag") {
        needUseExpandConfig(true)
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()
        saturationType = GLES20.glGetUniformLocation(mGLProgram, "uSaturation")
    }

    override fun onDrawExpandConfig() {
        super.onDrawExpandConfig()
        GLES20.glUniform1f(saturationType, saturationCode)
    }
}