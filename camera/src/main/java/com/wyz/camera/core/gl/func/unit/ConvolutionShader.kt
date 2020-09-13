package com.wyz.camera.core.gl.func.unit

import android.content.res.Resources
import com.wyz.camera.core.gl.BaseShader

//convolution
class ConvolutionShader : BaseShader {

    constructor(resource: Resources) : super(resource, "shader/color/basePlus.vert", "shader/color/convolution.frag") {
        needUseExpandConfig(true)
    }
}