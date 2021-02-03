package com.atom.camera.core.gl

import com.atom.camera.api.Renderer


class LazyWrapShader(renderer : Renderer?) : AbsWrapShader(renderer) {

    override fun getDefault(): BaseShader {
        return LazyShader()
    }
}