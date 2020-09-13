package com.wyz.camera.core.gl.func.unit

import android.content.res.Resources
import android.opengl.GLES20
import com.wyz.camera.core.gl.BaseShader

/**
 * 浮雕
 */
class CameoShader : BaseShader {

    protected var mGLCameo = 0
    protected var mCameoData: FloatArray

    constructor(resource: Resources, cameo: FloatArray = floatArrayOf(1920f, 1080f)) : super(resource, "shader/base.vert", "shader/color/cameo.frag") {
        needUseExpandConfig(true)
        this.mCameoData = cameo
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()
        mGLCameo = GLES20.glGetUniformLocation(mGLProgram, "uCameo")
    }

    override fun onDrawExpandConfig() {
        super.onDrawExpandConfig()
        GLES20.glUniform2fv(mGLCameo, 1, mCameoData, 0)
    }
}