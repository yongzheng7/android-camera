package com.wyz.camera.core.gl.func.unit

import android.content.res.Resources
import com.wyz.camera.core.gl.BaseShader

/**
 * 灰度滤镜
 */
class GrayShader : BaseShader {
    constructor(resource: Resources) : super(resource, "shader/base.vert", "shader/color/gray.frag")
}