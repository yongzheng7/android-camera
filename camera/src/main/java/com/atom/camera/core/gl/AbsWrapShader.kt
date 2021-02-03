package com.atom.camera.core.gl

import com.atom.camera.api.Renderer

abstract class AbsWrapShader : Renderer {

    companion object {
        // 逆时针
        val TYPE_7193 = 0
        val TYPE_1379 = 1
        val TYPE_3917 = 2
        val TYPE_9731 = 3

        // 顺时针
        val TYPE_1739 = 4
        val TYPE_3197 = 5
        val TYPE_9371 = 6
        val TYPE_7913 = 7
    }

    private var mRenderer: Renderer?
    private var mDefault: BaseShader

    constructor(renderer: Renderer?) {
        mRenderer = renderer
        mDefault = getDefault()
        setFlag(TYPE_7193)
    }

    protected abstract fun getDefault(): BaseShader

    fun getRenderer(): Renderer? {
        return mRenderer
    }

    /**
     * TYPE_MOVE
     *  -1f,1f[7]      0f,1f[8]        1f,1f[9]
     *
     *
     *  -1f,0f[4]------0f,0f[5]--------1f,0f[6]
     *
     *
     *  -1f,-1f[1]     0f,-1f[2]       1f,-1f[3]
     */
    fun setFlag(flag: Int) {
        if (flag == TYPE_7193) {
            mDefault.setVertexCo(floatArrayOf(
                    -1.0f, 1.0f,
                    -1.0f, -1.0f,
                    1.0f, 1.0f,
                    1.0f, -1.0f))
        } else if (flag == TYPE_1379) {
            mDefault.setVertexCo(floatArrayOf(
                    -1.0f, -1.0f,
                    1.0f, -1.0f,
                    -1.0f, 1.0f,
                    1.0f, 1.0f))
        } else if (flag == TYPE_3917) {
            mDefault.setVertexCo(floatArrayOf(
                    1.0f, -1.0f,
                    1.0f, 1.0f,
                    -1.0f, -1.0f,
                    -1.0f, 1.0f))
        } else if (flag == TYPE_9731) {
            mDefault.setVertexCo(floatArrayOf(
                    1.0f, 1.0f,
                    -1.0f, 1.0f,
                    1.0f, -1.0f,
                    -1.0f, -1.0f))
        } else if (flag == TYPE_1739) {
            mDefault.setVertexCo(floatArrayOf(
                    -1.0f, -1.0f,
                    -1.0f, 1.0f,
                    1.0f, -1.0f,
                    1.0f, 1.0f))
        } else if (flag == TYPE_3197) {
            mDefault.setVertexCo(floatArrayOf(
                    1.0f, -1.0f,
                    -1.0f, -1.0f,
                    1.0f, 1.0f,
                    -1.0f, 1.0f))
        } else if (flag == TYPE_9371) {
            mDefault.setVertexCo(floatArrayOf(
                    1.0f, 1.0f,
                    1.0f, -1.0f,
                    -1.0f, 1.0f,
                    -1.0f, -1.0f))
        } else if (flag == TYPE_7913) {
            mDefault.setVertexCo(floatArrayOf(
                    -1.0f, 1.0f,
                    1.0f, 1.0f,
                    -1.0f, -1.0f,
                    1.0f, -1.0f))
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

    override fun drawToTexture(texture: Int): Int {
        return mRenderer?.let {
            it.drawToTexture(mDefault.drawToTexture(texture))
        } ?: let {
            mDefault.drawToTexture(texture)
        }
    }

    override fun destroy() {
        mRenderer?.destroy()
        mDefault.destroy()
    }
}