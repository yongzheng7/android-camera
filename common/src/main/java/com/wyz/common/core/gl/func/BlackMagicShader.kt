package com.wyz.common.core.gl.func

import android.content.res.Resources
import com.wyz.common.core.gl.GroupShader
import com.wyz.common.core.gl.func.unit.BaseFuncShader
import com.wyz.common.core.gl.func.unit.BaseFuncShader.Companion.FILTER_SOBEL
import com.wyz.common.core.gl.func.unit.GrayShader

class BlackMagicShader : GroupShader{

    constructor(resources: Resources) :super(resources)

    override fun initBuffer() {
        super.initBuffer()
        addFilter(GrayShader(mRes!!))
        addFilter(BaseFuncShader(mRes!!, FILTER_SOBEL))
    }
}