package com.wyz.camera.core.gl.func.unit

import android.content.res.Resources
import android.opengl.GLES20
import com.wyz.camera.core.gl.BaseShader

/**
 * 复古
 */
class SepiaShader : BaseShader {

    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/sepia.frag") {

    }

}