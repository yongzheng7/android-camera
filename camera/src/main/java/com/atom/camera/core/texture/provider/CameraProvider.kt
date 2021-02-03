package com.atom.camera.core.texture.provider

import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.hardware.Camera
import android.os.Build
import android.os.Handler
import android.os.Message
import com.atom.camera.api.ITextureProvider
import java.io.IOException
import java.util.concurrent.Semaphore

class CameraProvider : ITextureProvider<SurfaceTexture> {

    private var mCamera: Camera? = null
    private val cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    private var parameters: Camera.Parameters? = null

    private val frameListener = OnFrameAvailableListener {
        mFrameSem?.drainPermits()
        mFrameSem?.release()
    }

    private var mFrameSem: Semaphore? = null

    @SuppressLint("HandlerLeak")
    private val mCameraAutoFocus: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            mCamera?.autoFocus { success, camera ->
                if (success) {
                    camera.cancelAutoFocus()
                    doAutoFocus()
                }
            }
        }
    }

    private fun requestCameraFocus() {
        if (mCamera != null && cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraAutoFocus.sendEmptyMessageDelayed(1, 1000)
        }
    }

    private fun doAutoFocus() {
        parameters = mCamera?.parameters
        parameters?.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO)
        mCamera?.parameters = parameters
        mCamera?.autoFocus { success, camera ->
            if (success) {
                camera.cancelAutoFocus() // 只有加上了这一句，才会自动对焦。
                if (Build.MODEL != "KORIDY H30") {
                    parameters = camera.parameters
                    parameters?.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                    camera.parameters = parameters
                } else {
                    parameters = camera.parameters
                    parameters?.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO)
                    camera.parameters = parameters
                }
            }
        }
    }

    override fun open(surface: SurfaceTexture): Point {
        val size = Point()
        try {
            mCamera = Camera.open(cameraId)
            mFrameSem = Semaphore(0)
            mCamera?.setPreviewTexture(surface)
            surface.setOnFrameAvailableListener(frameListener)
            mCamera?.getParameters()?.previewSize?.also {
                size.x = it.height
                size.y = it.width
            }
            mCamera?.startPreview()
            requestCameraFocus()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return size
    }

    override fun close() {
        mFrameSem?.drainPermits()
        mFrameSem?.release()
        mFrameSem = null

        mCamera?.stopPreview()
        mCamera?.release()
        mCamera = null
    }

    override fun frame(): Boolean {
        try {
            mFrameSem?.acquire()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }

    override fun getTimeStamp(): Long {
        return -1
    }

    override fun isLandscape(): Boolean {
        return true
    }
}