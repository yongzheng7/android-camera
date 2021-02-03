package com.atom.camera.core.gl.yuv

import android.content.res.Resources
import android.opengl.GLES20
import com.atom.camera.core.gl.BaseShader
import com.atom.camera.core.gl.LazyShader
import com.atom.camera.utils.MatrixUtils.Companion.TYPE_CENTERCROP
import com.atom.camera.utils.MatrixUtils.Companion.getMatrix
import java.nio.ByteBuffer

class YuvOutputShader : BaseShader {

    companion object {
        val EXPORT_TYPE_I420 = 1
        val EXPORT_TYPE_YV12 = 2
        val EXPORT_TYPE_NV12 = 3
        val EXPORT_TYPE_NV21 = 4
    }

    private var mTempBuffer: ByteBuffer? = null
    private val lastViewPort = IntArray(4)

    private var mExportFilter: BaseShader
    private var mScaleFilter: LazyShader? = null

    constructor(type: Int) : super(null, "None", "None") {
        mExportFilter = ExportShader(type)
    }

    constructor(res: Resources, yuvShader: String) : super(null, "None", "None") {
        mExportFilter = ExportShader(res, yuvShader)
    }

    override fun getTextureMatrix(): FloatArray {
        return mExportFilter.getTextureMatrix()
    }

    override fun create() {
        mExportFilter.create()
    }

    override fun sizeChanged(width: Int, height: Int) {
        mExportFilter.sizeChanged(width, height)
        mTempBuffer = ByteBuffer.allocate(mWidth * mHeight * 3 / 2)
    }

    fun setInputTextureSize(width: Int, height: Int) {
        runOnGLThread(Runnable {
            mScaleFilter = LazyShader()
            mScaleFilter?.create()
            mScaleFilter?.sizeChanged(mWidth, mHeight)
            getMatrix(mScaleFilter!!.getVertexMatrix(), TYPE_CENTERCROP, width, height, mWidth, mHeight)
        })
    }

    override fun draw(texture: Int) {
        onTaskExec()
        val isBlend = GLES20.glIsEnabled(GLES20.GL_BLEND)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, lastViewPort, 0)
        GLES20.glViewport(0, 0, mWidth, mHeight)
        if (mScaleFilter != null) {
            mExportFilter.draw(mScaleFilter!!.drawToTexture(texture))
        } else {
            mExportFilter.draw(texture)
        }
        GLES20.glReadPixels(0, 0, mWidth, mHeight * 3 / 8, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mTempBuffer)
        GLES20.glViewport(lastViewPort[0], lastViewPort[1], lastViewPort[2], lastViewPort[3])
        if (isBlend) {
            GLES20.glEnable(GLES20.GL_BLEND)
        }
    }

    fun getOutput(data: ByteArray, offset: Int, length: Int) {
        mTempBuffer?.get(data, offset, length)
        mTempBuffer?.clear()
    }

}