package com.atom.camera.core.gl

import com.atom.camera.api.Renderer
import com.atom.camera.core.gl.oes.OesShader


class OesWrapShader(renderer : Renderer?) : AbsWrapShader(renderer) {

    override fun getDefault(): BaseShader {
        return OesShader()
    }
}