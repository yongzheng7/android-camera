package com.wyz.common.core.gl.func

import android.content.res.Resources
import com.wyz.common.core.gl.GroupShader
import com.wyz.common.core.gl.func.unit.BaseFuncShader
import com.wyz.common.core.gl.func.unit.BaseFuncShader.Companion.FILTER_GAUSS
import com.wyz.common.core.gl.func.unit.BaseFuncShader.Companion.FILTER_SOBEL
import com.wyz.common.core.gl.func.unit.GrayShader

class CandyShader : GroupShader {
    constructor(resource: Resources) : super(resource)

    override fun initBuffer() {
        super.initBuffer()
        addFilter(GrayShader(mRes!!))
        addFilter(BaseFuncShader(mRes!!, FILTER_GAUSS))
        addFilter(BaseFuncShader(mRes!!, FILTER_SOBEL))
    }

}