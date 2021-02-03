package com.atom.camera.core.gl.func.unit

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.camera.core.gl.BaseShader

/**
 * 对比度滤镜
 */
class ContrastShader : BaseShader {

    private var contrastType = 0
    private val contrastCode = 3.0f

    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/contrast.frag") {
        needUseExpandConfig(true)
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()

        contrastType = GLES20.glGetUniformLocation(mGLProgram, "uStepcv")
    }

    override fun onDrawExpandConfig() {
        super.onDrawExpandConfig()

        GLES20.glUniform1f(contrastType, contrastCode)
    }

}