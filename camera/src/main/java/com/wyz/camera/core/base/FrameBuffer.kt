package com.wyz.camera.core.base

import android.opengl.GLES20

class FrameBuffer {

    private var mFrameTemp: IntArray? = null
    private var lastWidth = 0
    private var lastHeight = 0

    fun bindFrameBuffer(width: Int, height: Int): Int {
        return bindFrameBuffer(width, height, false)
    }

    fun bindFrameBuffer(width: Int, height: Int, hasRenderBuffer: Boolean): Int {
        if (lastWidth != width || lastHeight != height) {
            destroyFrameBuffer()
            lastWidth = width
            lastHeight = height
        }
        return if (mFrameTemp == null) {
            createFrameBuffer(hasRenderBuffer, width, height, GLES20.GL_TEXTURE_2D, GLES20.GL_RGBA, GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE)
        } else {
            bindFrameBuffer()
        }
    }

    fun createFrameBuffer(hasRenderBuffer: Boolean, width: Int, height: Int, texType: Int, texFormat: Int,
                          minParams: Int, maxParams: Int, wrapS: Int, wrapT: Int): Int {
        mFrameTemp = IntArray(4)
        GLES20.glGenFramebuffers(1, mFrameTemp, 0)
        GLES20.glGenTextures(1, mFrameTemp, 1)
        GLES20.glBindTexture(texType, mFrameTemp!![1])
        GLES20.glTexImage2D(texType, 0, texFormat, width, height,
                0, texFormat, GLES20.GL_UNSIGNED_BYTE, null)
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameteri(texType, GLES20.GL_TEXTURE_MIN_FILTER, minParams)
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameteri(texType, GLES20.GL_TEXTURE_MAG_FILTER, maxParams)
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(texType, GLES20.GL_TEXTURE_WRAP_S, wrapS)
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(texType, GLES20.GL_TEXTURE_WRAP_T, wrapT)
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mFrameTemp, 3)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameTemp!![0])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                texType, mFrameTemp!![1], 0)
        if (hasRenderBuffer) {
            GLES20.glGenRenderbuffers(1, mFrameTemp, 2)
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mFrameTemp!![2])
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height)
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mFrameTemp!![2])
        }
        return GLES20.glGetError()
    }

    fun bindFrameBuffer(): Int {
        if (mFrameTemp == null) {
            return -1
        }
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mFrameTemp, 3)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameTemp!![0])
        return GLES20.glGetError()
    }

    fun unBindFrameBuffer() {
        if (mFrameTemp != null) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameTemp!![3])
        }
    }

    fun getCacheTextureId(): Int {
        return if (mFrameTemp != null) mFrameTemp!![1] else -1
    }


    fun destroyFrameBuffer() {
        if (mFrameTemp != null) {
            GLES20.glDeleteFramebuffers(1, mFrameTemp, 0)
            GLES20.glDeleteTextures(1, mFrameTemp, 1)
            if (mFrameTemp!![2] > 0) {
                GLES20.glDeleteRenderbuffers(1, mFrameTemp, 2)
            }
            mFrameTemp = null
        }
    }
}