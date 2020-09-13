package com.wyz.camera.core.gl.func.unit

import android.content.res.Resources
import com.wyz.camera.core.gl.BaseShader


class ToonShader : BaseShader {

    constructor(resource: Resources) : super(resource, "shader/color/basePlus.vert", "shader/color/toon.frag") {
        needUseExpandConfig(true)
    }
}