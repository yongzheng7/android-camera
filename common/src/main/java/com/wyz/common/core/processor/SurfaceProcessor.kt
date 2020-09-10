package com.wyz.common.core.processor

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLES20
import com.wyz.common.api.IObserver
import com.wyz.common.api.ITextureProvider
import com.wyz.common.api.Renderer
import com.wyz.common.core.base.FrameBean
import com.wyz.common.core.base.Observable
import com.wyz.common.core.egl.EGLConfigAttrs
import com.wyz.common.core.egl.EGLContextAttrs
import com.wyz.common.core.egl.EGLHelper
import com.wyz.common.core.base.FrameBuffer
import com.wyz.common.core.gl.WrapShader
import com.wyz.common.utils.TextureUtils.Companion.createTextureID

class SurfaceProcessor {

    private var mGLThreadFlag = false

    private var mGLThread: Thread? = null

    private var mRenderer: WrapShader? = null

    private val observable: Observable<FrameBean>

    private var mProvider: ITextureProvider? = null


    private val LOCK = Object()

    constructor() {
        observable = Observable()
    }

    fun setTextureProvider(provider: ITextureProvider) {
        mProvider = provider
    }

    fun setRenderer(renderer: Renderer) {
        mRenderer = WrapShader(renderer)
    }

    fun start() {
        synchronized(LOCK) {
            if (!mGLThreadFlag) {
                if (mProvider == null) {
                    return
                }
                mGLThreadFlag = true
                mGLThread = Thread(Runnable { glRun() })
                mGLThread?.start()
                try {
                    LOCK.wait()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stop() {
        synchronized(LOCK) {
            if (mGLThreadFlag) {
                mGLThreadFlag = false
                mProvider?.close()
                try {
                    LOCK.wait()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun glRun() {
        val iTextureProvider = mProvider ?: let {
            synchronized(LOCK) { LOCK.notifyAll() }
            return
        }
        val egl = EGLHelper()
        val ret = egl.createGLESWithSurface(EGLConfigAttrs(), EGLContextAttrs(), SurfaceTexture(1))
        if (!ret) {
            synchronized(LOCK) { LOCK.notifyAll() }
            return
        }
        val mInputSurfaceTextureId = createTextureID(true) // 创建一个屏幕纹理id
        val mInputSurfaceTexture = SurfaceTexture(mInputSurfaceTextureId)
        val size = iTextureProvider.open(mInputSurfaceTexture) ?: return
        if (size.x <= 0 || size.y <= 0) {
            destroyGL(egl)
            synchronized(LOCK) { LOCK.notifyAll() }
            return
        }
        val mSourceWidth = size.x
        val mSourceHeight = size.y
        synchronized(LOCK) { LOCK.notifyAll() }
        //要求数据源提供者必须同步返回数据大小
        if (mSourceWidth <= 0 || mSourceHeight <= 0) {
            destroyGL(egl)
            return
        }
        if (mRenderer == null) {
            mRenderer = WrapShader(null)
        }
        mRenderer?.create()
        mRenderer?.sizeChanged(mSourceWidth, mSourceHeight)
        mRenderer?.setFlag(if (iTextureProvider.isLandscape()) WrapShader.TYPE_CAMERA else WrapShader.TYPE_MOVE)

        //用于其他的回调
        val rb = FrameBean()
        rb.egl = egl
        rb.sourceWidth = mSourceWidth
        rb.sourceHeight = mSourceHeight
        rb.endFlag = false
        rb.threadId = Thread.currentThread().id
        val sourceFrame = FrameBuffer()
        //要求数据源必须同步填充SurfaceTexture，填充完成前等待
        while (!iTextureProvider.frame() && mGLThreadFlag) {
            mInputSurfaceTexture.updateTexImage()
            mInputSurfaceTexture.getTransformMatrix(mRenderer?.getTextureMatrix())

            sourceFrame.bindFrameBuffer(mSourceWidth, mSourceHeight)
            GLES20.glViewport(0, 0, mSourceWidth, mSourceHeight)
            mRenderer?.draw(mInputSurfaceTextureId)
            sourceFrame.unBindFrameBuffer()

            //接收数据源传入的时间戳
            rb.textureId = sourceFrame.cacheTextureId
            rb.timeStamp = iTextureProvider.getTimeStamp()
            rb.textureTime = mInputSurfaceTexture.timestamp
            observable.notify(rb)
        }
        synchronized(LOCK) {
            rb.endFlag = true
            observable.notify(rb)
            mRenderer?.destroy()
            destroyGL(egl)
            LOCK.notifyAll()
        }
    }


    private fun destroyGL(egl: EGLHelper) {
        mGLThreadFlag = false
        EGL14.eglMakeCurrent(
                egl.getDisplay(),
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroyContext(egl.getDisplay(), egl.getDefaultContext())
        EGL14.eglTerminate(egl.getDisplay())
    }

    fun addObserver(observer: IObserver<FrameBean>) {
        observable.addObserver(observer)
    }
}