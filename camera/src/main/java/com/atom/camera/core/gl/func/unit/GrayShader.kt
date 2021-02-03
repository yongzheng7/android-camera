package com.atom.camera.core.gl.func.unit

import android.content.res.Resources
import com.atom.camera.core.gl.BaseShader

/**
 * 灰度滤镜
 */
class GrayShader : BaseShader {
    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/gray.frag")
}