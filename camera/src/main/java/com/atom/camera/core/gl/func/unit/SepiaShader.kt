package com.atom.camera.core.gl.func.unit

import android.content.res.Resources
import com.atom.camera.core.gl.BaseShader

/**
 * 复古
 */
class SepiaShader : BaseShader {

    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/sepia.frag") {

    }

}