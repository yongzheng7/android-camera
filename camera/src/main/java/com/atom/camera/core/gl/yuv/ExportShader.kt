package com.atom.camera.core.gl.yuv

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.camera.core.gl.BaseShader

class ExportShader : BaseShader {
    private var mGLWidth = 0
    private var mGLHeight = 0

    constructor(resource: Resources, frag: String) : super(resource, "shader/base.vert", "shader/convert/$frag")

    constructor(type: Int) :super(null, BASE_VERT, ExportSource.getFrag(type))

    override fun onCreate() {
        super.onCreate()
        mGLWidth = GLES20.glGetUniformLocation(mGLProgram, "uWidth")
        mGLHeight = GLES20.glGetUniformLocation(mGLProgram, "uHeight")
    }

    override fun onSetExpandData() {
        super.onSetExpandData()
        GLES20.glUniform1f(mGLWidth, mWidth.toFloat())
        GLES20.glUniform1f(mGLHeight, mHeight.toFloat())
    }
}