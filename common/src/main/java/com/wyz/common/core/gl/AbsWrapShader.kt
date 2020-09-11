package com.wyz.common.core.gl

import com.wyz.common.api.Renderer
import com.wyz.common.core.gl.oes.OesShader

abstract class AbsWrapShader : Renderer {

    companion object {
        val TYPE_MOVE = 0
        val TYPE_CAMERA = 1
    }

    private var mRenderer: Renderer?
    private var mDefault: BaseShader

    constructor(renderer: Renderer?) {
        mRenderer = renderer
        mDefault = getDefault()
        setFlag(TYPE_MOVE)
    }

    abstract fun getDefault(): BaseShader

    fun setFlag(flag: Int) {
        if (flag == TYPE_MOVE) {
            mDefault.setVertexCo(floatArrayOf(
                    -1.0f, 1.0f,
                    -1.0f, -1.0f,
                    1.0f, 1.0f,
                    1.0f, -1.0f))
        } else if (flag == TYPE_CAMERA) {
            mDefault.setVertexCo(floatArrayOf(
                    -1.0f, -1.0f,
                    1.0f, -1.0f,
                    -1.0f, 1.0f,
                    1.0f, 1.0f))
        }
    }

    fun getTextureMatrix(): FloatArray {
        return mDefault.getTextureMatrix()
    }

    override fun create() {
        mDefault.create()
        mRenderer?.create()
    }

    override fun sizeChanged(width: Int, height: Int) {
        mDefault.sizeChanged(width, height)
        mRenderer?.sizeChanged(width, height)
    }

    override fun draw(texture: Int) {
        mRenderer?.also {
            it.draw(mDefault.drawToTexture(texture))
        } ?: also {
            mDefault.draw(texture)
        }
    }

    override fun destroy() {
        mRenderer?.destroy()
        mDefault.destroy()
    }
}