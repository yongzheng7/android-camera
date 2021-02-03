package com.atom.camera.core.gl.func.unit

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.camera.core.gl.BaseShader

/**
 * 亮度滤镜
 */
class BrightnessShadser :BaseShader{

    private var uBrightness = 0
    private val brightnessCode = 0.1f

    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/brightness.frag") {
        needUseExpandConfig(true)
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()
        uBrightness = GLES20.glGetUniformLocation(mGLProgram, "uBrightness")
    }

    override fun onDrawExpandConfig() {
        super.onDrawExpandConfig()
        GLES20.glUniform1f(uBrightness, brightnessCode)
    }
}