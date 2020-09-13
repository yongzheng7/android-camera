package com.wyz.camera.core.gl

import com.wyz.camera.api.Renderer


class LazyWrapShader(renderer : Renderer?) : AbsWrapShader(renderer) {

    override fun getDefault(): BaseShader {
        return LazyShader()
    }
}