package com.atom.camera.core.processor

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLES20
import android.util.Log
import com.atom.camera.api.IObserver
import com.atom.camera.api.ITextureProvider
import com.atom.camera.api.Renderer
import com.atom.camera.core.base.FrameBean
import com.atom.camera.core.base.Observable
import com.atom.camera.core.egl.EGLConfigAttrs
import com.atom.camera.core.egl.EGLContextAttrs
import com.atom.camera.core.egl.EGLHelper
import com.atom.camera.core.base.FrameBuffer
import com.atom.camera.core.gl.AbsWrapShader
import com.atom.camera.core.gl.GroupShader
import com.atom.camera.core.gl.LazyWrapShader
import java.lang.RuntimeException

class PictureProcessor {

    private var mGLThreadFlag = false

    private var mGLThread: Thread? = null

    private var mRenderer: AbsWrapShader? = null

    private val observable: Observable<FrameBean>

    private var mProvider: ITextureProvider<IntArray>? = null


    private val LOCK = Object()

    constructor() {
        observable = Observable()
    }

    fun setTextureProvider(provider: ITextureProvider<IntArray>) {
        mProvider = provider
    }

    fun setRenderer(renderer: Renderer) {
        if(mRenderer == null){
            mRenderer = LazyWrapShader(renderer)
        }else{
            val temp = mRenderer!!.getRenderer()
            if(temp is GroupShader){
                temp.addFilter(renderer)
            }
        }
    }

    fun isRunning(): Boolean = mGLThreadFlag

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
        val intArray = IntArray(1) // 创建一个屏幕纹理id
        intArray[0] = -1
        val size = iTextureProvider.open(intArray)
        if (size.x <= 0 || size.y <= 0) {
            destroyGL(egl)
            synchronized(LOCK) { LOCK.notifyAll() }
            return
        }
        if (intArray[0] == -1) {
            destroyGL(egl)
            synchronized(LOCK) { LOCK.notifyAll() }
            throw RuntimeException(" texture id = -1")
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
            mRenderer = LazyWrapShader(null)
        }
        Log.e("GLthread", "create >");
        mRenderer?.create()
        mRenderer?.sizeChanged(mSourceWidth, mSourceHeight)
        mRenderer?.setFlag(if (iTextureProvider.isLandscape()) AbsWrapShader.TYPE_1379 else AbsWrapShader.TYPE_1739)

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
            sourceFrame.bindFrameBuffer(mSourceWidth, mSourceHeight)
            GLES20.glViewport(0, 0, mSourceWidth, mSourceHeight)
            mRenderer?.draw(intArray[0])
            sourceFrame.unBindFrameBuffer()
            //接收数据源传入的时间戳
            rb.textureId = sourceFrame.getCacheTextureId()
            rb.timeStamp = iTextureProvider.getTimeStamp()
            rb.textureTime = System.currentTimeMillis()
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