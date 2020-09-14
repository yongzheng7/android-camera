package com.wyz.camera.core.gl.func.unit

import android.content.res.Resources
import android.opengl.GLES20
import com.wyz.camera.core.gl.BaseShader

/**
 * 暗角滤镜
 */
class VignetteShader : BaseShader{

    private var mVignetteCenterLocation = 0
    private var mVignetteColorLocation = 0
    private var mVignetteStartLocation = 0
    private var mVignetteEndLocation = 0


    private val vec2 = floatArrayOf(0.5f, 0.5f)
    private val mVignetteColor = floatArrayOf(0.0f, 0.0f, 0.0f)
    private val mVignetteStart = 0.35f
    private val mVignetteEnd = 0.75f
    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/vignette.frag") {
        needUseExpandConfig(true)
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()

        mVignetteCenterLocation = GLES20.glGetUniformLocation(mGLProgram, "vignetteCenter")
        mVignetteColorLocation = GLES20.glGetUniformLocation(mGLProgram, "vignetteColor")
        mVignetteStartLocation = GLES20.glGetUniformLocation(mGLProgram, "vignetteStart")
        mVignetteEndLocation = GLES20.glGetUniformLocation(mGLProgram, "vignetteEnd")
    }

    override fun onDrawExpandConfig() {
        super.onDrawExpandConfig()

        GLES20.glUniform2fv(mVignetteCenterLocation, 1, vec2, 0)
        GLES20.glUniform3fv(mVignetteColorLocation, 1, mVignetteColor , 0)
        GLES20.glUniform1f(mVignetteStartLocation, mVignetteStart)
        GLES20.glUniform1f(mVignetteEndLocation, mVignetteEnd)
    }
}