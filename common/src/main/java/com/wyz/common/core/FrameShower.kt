package com.wyz.common.core

import android.opengl.EGLSurface
import android.opengl.GLES20
import com.wyz.common.api.FrameDrawedListener
import com.wyz.common.api.IObserver
import com.wyz.common.core.base.FrameBean
import com.wyz.common.gl.BaseFilter
import com.wyz.common.gl.LazyFilter
import com.wyz.common.utils.MatrixUtils.Companion.TYPE_CENTERCROP
import com.wyz.common.utils.MatrixUtils.Companion.flip
import com.wyz.common.utils.MatrixUtils.Companion.getMatrix


open class FrameShower : IObserver<FrameBean> {

    private var mShowSurface: EGLSurface? = null
    private var mFilter: BaseFilter? = null
    private var mSurface: Any? = null
    private var isShow = false
    private var mWidth = 0
    private var mHeight = 0
    private var mMatrixType = TYPE_CENTERCROP
    private var mListener: FrameDrawedListener? = null

    open fun setOutputSize(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    open fun setSurface(surface: Any?) {
        mSurface = surface
    }

    fun setMatrixType(type: Int) {
        mMatrixType = type
    }

    open fun open() {
        isShow = true
    }

    open fun close() {
        isShow = false
    }


    override fun run(rb: FrameBean) {
        if (rb.endFlag && mShowSurface != null) {
            rb.egl?.destroySurface(mShowSurface)
            mShowSurface = null
        } else if (isShow && mSurface != null) {
            if (mShowSurface == null) {
                mShowSurface = rb.egl?.createWindowSurface(mSurface!!)
                val lazyFilter = LazyFilter()
                lazyFilter.create()
                lazyFilter.sizeChanged(rb.sourceWidth, rb.sourceHeight)
                getMatrix(lazyFilter.getVertexMatrix(), mMatrixType, rb.sourceWidth, rb.sourceHeight, mWidth, mHeight)
                flip(lazyFilter.getVertexMatrix(), false, true)
                mFilter = lazyFilter
            }
            rb.egl?.makeCurrent(mShowSurface)
            GLES20.glViewport(0, 0, mWidth, mHeight)
            mFilter?.draw(rb.textureId)
            mListener?.onDrawEnd(mShowSurface, rb)
            rb.egl?.swapBuffers(mShowSurface)
        }
    }

    /**
     * 设置单帧渲染完成监听器
     * @param listener 监听器
     */
    open fun setOnDrawEndListener(listener: FrameDrawedListener) {
        mListener = listener
    }

}