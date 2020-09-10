package com.wyz.common.core.gl.func.unit

import android.content.res.Resources
import com.wyz.common.core.gl.BaseShader

class BaseFuncShader : BaseShader{
    companion object{
        val FILTER_SOBEL = "shader/func/sobel.frag"
        val FILTER_SOBEL_REVERSE = "shader/func/sobel2.frag"
        val FILTER_GAUSS = "shader/func/gauss.frag"
    }

    constructor(resource: Resources, fragment: String) : super(resource, "shader/base.vert", fragment) {
        needUseExpandConfig(true)
    }
}