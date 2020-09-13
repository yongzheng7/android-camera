package com.wyz.camera.core.gl.func.unit

import android.content.res.Resources
import android.opengl.GLES20
import com.wyz.camera.core.gl.BaseShader

class InvertShader : BaseShader {

    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/invert.frag") {

    }

}