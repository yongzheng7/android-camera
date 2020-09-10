package com.wyz.common.core.gl.mark

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.wyz.common.core.gl.LazyShader
import com.wyz.common.utils.TextureUtils.Companion.createTextureID

class WaterMarkShader : LazyShader() {

    private val viewPort = IntArray(4)
    private val markPort = IntArray(4)
    private val mark: LazyShader = object : LazyShader() {
        override fun onClear() {}
    }
    private var markTextureId = -1

    fun setMarkPosition(x: Int, y: Int, width: Int, height: Int): WaterMarkShader {
        markPort[0] = x
        markPort[1] = y
        markPort[2] = width
        markPort[3] = height
        runOnGLThread(Runnable { mark.sizeChanged(width, height) })
        return this
    }

    fun setMark(bmp: Bitmap?): WaterMarkShader {
        runOnGLThread(Runnable {
            if (bmp != null) {
                if (markTextureId == -1) {
                    markTextureId = createTextureID(false)
                } else {
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, markTextureId)
                }
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
                bmp.recycle()
            } else {
                if (markTextureId != -1) {
                    GLES20.glDeleteTextures(1, intArrayOf(markTextureId), 0)
                }
            }
        })
        return this
    }

    override fun create() {
        super.create()
        mark.create()
    }

    override fun onDraw() {
        super.onDraw()
        if (markTextureId != -1) {
            GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewPort, 0)// 获取之前的绘制大小
            GLES20.glViewport(markPort[0], mHeight - markPort[3] - markPort[1], markPort[2], markPort[3]) // 进行设置大小
            GLES20.glEnable(GLES20.GL_BLEND) // 设置混合
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA) //设置混合方式
            GLES20.glBlendEquation(GLES20.GL_FUNC_ADD) // 设置对比
            mark.draw(markTextureId) // 进行绘制
            GLES20.glDisable(GLES20.GL_BLEND) // 取消混合
            GLES20.glViewport(viewPort[0], viewPort[1], viewPort[2], viewPort[3]) // 复原绘制大小
        }
    }
}