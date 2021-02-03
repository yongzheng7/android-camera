package com.atom.camera.core.gl.func.unit

import android.content.res.Resources
import com.atom.camera.core.gl.BaseShader

class InvertShader : BaseShader {

    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/invert.frag") {

    }

}