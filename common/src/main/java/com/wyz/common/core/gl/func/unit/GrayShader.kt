package com.wyz.common.core.gl.func.unit

import android.content.res.Resources
import com.wyz.common.core.gl.BaseShader

class GrayShader : BaseShader {
    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/gray.frag")
}