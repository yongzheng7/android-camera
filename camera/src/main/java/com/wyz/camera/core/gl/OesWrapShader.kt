package com.wyz.camera.core.gl

import com.wyz.camera.api.Renderer
import com.wyz.camera.core.gl.oes.OesShader


class OesWrapShader(renderer : Renderer?) : AbsWrapShader(renderer) {

    override fun getDefault(): BaseShader {
        return OesShader()
    }
}