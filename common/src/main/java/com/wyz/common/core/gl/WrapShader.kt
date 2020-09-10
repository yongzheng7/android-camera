package com.wyz.common.core.gl

import com.wyz.common.api.Renderer
import com.wyz.common.core.gl.oes.OesShader

class WrapShader : Renderer {

    companion object {
        val TYPE_MOVE = 0
        val TYPE_CAMERA = 1
    }

    private var mRenderer: Renderer?
    private var mFilter: OesShader

    constructor(renderer: Renderer?) {
        mRenderer = renderer
        mFilter = OesShader()
        setFlag(TYPE_MOVE)
    }

    fun setFlag(flag: Int) {
        if (flag == TYPE_MOVE) {
            mFilter.setVertexCo(floatArrayOf(
                    -1.0f, 1.0f,
                    -1.0f, -1.0f,
                    1.0f, 1.0f,
                    1.0f, -1.0f))
        } else if (flag == TYPE_CAMERA) {
            mFilter.setVertexCo(floatArrayOf(
                    -1.0f, -1.0f,
                    1.0f, -1.0f,
                    -1.0f, 1.0f,
                    1.0f, 1.0f))
        }
    }

    fun getTextureMatrix(): FloatArray {
        return mFilter.getTextureMatrix()
    }

    override fun create() {
        mFilter.create()
        mRenderer?.create()
    }

    override fun sizeChanged(width: Int, height: Int) {
        mFilter.sizeChanged(width, height)
        mRenderer?.sizeChanged(width, height)
    }

    override fun draw(texture: Int) {
        mRenderer?.also {
            it.draw(mFilter.drawToTexture(texture))
        } ?: also {
            mFilter.draw(texture)
        }
    }

    override fun destroy() {
        mRenderer?.destroy()
        mFilter.destroy()
    }
}