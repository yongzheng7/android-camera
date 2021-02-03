package com.atom.camera.core.gl.func.unit

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.camera.core.gl.BaseShader

//convolution
class ConvolutionShader : BaseShader {
    private var mGLTexWidth = 0
    private var mGLTexHeight = 0

    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/convolution.frag") {
        needUseExpandConfig(true)
    }

    override fun onCreateExpandConfig() {
        super.onCreateExpandConfig()
        mGLTexWidth = GLES20.glGetUniformLocation(mGLProgram, "uTexWidth")
        mGLTexHeight = GLES20.glGetUniformLocation(mGLProgram, "uTexHeight")
    }

    override fun onDrawExpandConfig() {
        super.onDrawExpandConfig()
        GLES20.glUniform1f(mGLTexWidth, 1.3f / mWidth)
        GLES20.glUniform1f(mGLTexHeight, 1.3f /mHeight)
    }
}