package com.atom.camera.core.gl.func

import android.content.res.Resources
import com.atom.camera.core.gl.GroupShader
import com.atom.camera.core.gl.func.unit.BaseFuncShader
import com.atom.camera.core.gl.func.unit.BaseFuncShader.Companion.FILTER_GAUSS
import com.atom.camera.core.gl.func.unit.BaseFuncShader.Companion.FILTER_SOBEL_REVERSE
import com.atom.camera.core.gl.func.unit.GrayShader

class StickFigureShader : GroupShader {

    constructor(resources: Resources) : super(resources)

    init {
        addFilter(GrayShader(mRes!!))
        addFilter(BaseFuncShader(mRes!!, FILTER_GAUSS))
        addFilter(BaseFuncShader(mRes!!, FILTER_SOBEL_REVERSE))
    }

}