package com.wyz.common.core.gl

import android.content.res.Resources
import android.opengl.GLES20

class RollShader : LazyShader {

    private var mXRollTime = 10
    private var mYRollTime = 10
    private var mFrameCount = 0

    constructor(resource: Resources) : super(resource)

    constructor(vert: String, frag: String) : super(vert, frag)

    fun setRollTime(xTime: Int, yTime: Int) {
        mXRollTime = xTime
        mYRollTime = yTime
    }

    override fun onCreate() {
        super.onCreate()
        mFrameCount = 0
    }

    override fun onDraw() {
        mFrameCount++
        if (mFrameCount >= (mXRollTime + mYRollTime)) {
            mFrameCount = 0
        }
        if (mFrameCount < mXRollTime) {
            //todo x方向滚动
            val shift = mFrameCount * mWidth / mXRollTime / 2
            for (i in 0..2) {
                GLES20.glViewport(mWidth * i / 2 - shift, 0, mWidth / 2, mHeight / 2)
                super.onDraw()
                GLES20.glViewport(mWidth * i / 2 + shift - mWidth / 2, mHeight / 2, mWidth / 2, mHeight / 2)
                super.onDraw()
            }
        } else {
            //todo y方向滚动
            val shift = (mHeight - (mFrameCount - mXRollTime) * mHeight / mYRollTime) / 2
            for (i in 0..2) {
                GLES20.glViewport(0, mHeight * i / 2 - shift, mWidth / 2, mHeight / 2)
                super.onDraw()
                GLES20.glViewport(mWidth / 2, mHeight * i / 2 + shift - mHeight / 2, mWidth / 2, mHeight / 2)
                super.onDraw()
            }
        }
    }
}