package com.wyz.common.core.gl

import com.wyz.common.api.Renderer
import com.wyz.common.core.gl.oes.OesShader


class OesWrapShader(renderer : Renderer?) : AbsWrapShader(renderer) {

    override fun getDefault(): BaseShader {
        return OesShader()
    }
}