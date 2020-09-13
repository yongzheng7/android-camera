package com.wyz.camera.core.gl.func

import android.content.res.Resources
import com.wyz.camera.core.gl.GroupShader
import com.wyz.camera.core.gl.func.unit.BaseFuncShader
import com.wyz.camera.core.gl.func.unit.BaseFuncShader.Companion.FILTER_SOBEL
import com.wyz.camera.core.gl.func.unit.GrayShader

class BlackMagicShader : GroupShader {

    constructor(resources: Resources) : super(resources)

    init {
        addFilter(GrayShader(mRes!!))
        addFilter(BaseFuncShader(mRes!!, FILTER_SOBEL))
    }
}