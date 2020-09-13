package com.wyz.camera.test

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.*
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import com.wyz.camera.api.Renderer
import com.wyz.camera.core.gl.GroupShader
import com.wyz.camera.utils.MatrixUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class CameraView : TextureView, SurfaceTextureListener, Renderer {

    enum class State {
        START, RECORDING, PAUSE, STOP
    }

    // camera
    private var mCameraWidth: Int = 0
    private var mCameraHeight = 0
    private var parameters: Camera.Parameters? = null

    // filter
    // 滤镜系统
    private var mCurrentFilterIndex: Int = 0// 当前滤镜
    private var mFilter : GroupShader? = null  // 基础滤镜

    // recordhelper
    // 录制系统
    private var cameraRecorder: CameraRecorder? = null //GP录像类

    private var cameraRecorderState: State = State.START

    // 摄像头
    private var mCamera: Camera? = null
    private var mCameraCount = 0
    private var mCurrentCameraId =
            Camera.CameraInfo.CAMERA_FACING_FRONT // 设置默认的摄像头为前置

    // 录制尺寸 注意每个手机支持的录制尺寸可能会有问题
    var mCameraAutoFocusCallbackHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (mCamera != null) {
                mCamera!!.autoFocus { success, camera ->
                    if (success) {
                        camera.cancelAutoFocus()
                        doAutoFocus()
                    }
                }
            }
        }
    }


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    )

    init {
        // 获取摄像头个数
        mCameraCount = Camera.getNumberOfCameras()
        mFilter = GroupShader(resources)
        // 初始化录制
        cameraRecorder = CameraRecorder()
        // 设置监听
        surfaceTextureListener = this
    }

    /*-------------------------------------------摄像头管理---------------------------------------------*/
    /**
     * 切换摄像头
     */
    fun switchCamera() {
        if (mCameraCount > 1) {
            mCurrentCameraId =
                    if (mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) Camera.CameraInfo.CAMERA_FACING_BACK else Camera.CameraInfo.CAMERA_FACING_FRONT
            stopPreview()
            mCamera = Camera.open(mCurrentCameraId)
            cameraRecorder?.startPreview()
            requestCameraFocus()
        }
    }

    /*-------------------------------------------滤镜管理---------------------------------------------*/


    /*-------------------------------------------拍照管理---------------------------------------------*/

    open fun takeCarema() {
        mCamera?.takePicture(
                null,
                null,
                Camera.PictureCallback { p0, p1 -> SavePictureTask().execute(p0) });
    }

    /*-------------------------------------------录制管理---------------------------------------------*/
    // 开始进行记录
    fun startRecord(filePath: String?) {
        cameraRecorder?.setOutputPath(filePath)
        try {
            cameraRecorder?.startRecord()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        requestCameraFocus()
        cameraRecorderState = State.RECORDING
    }

    // 开始预览
    fun startPreview() {
        if (mCamera == null) {
            mCamera = Camera.open(mCurrentCameraId)
            requestCameraFocus()
        }
        try {
            cameraRecorder?.startPreview()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // 停止预览
    fun stopPreview() {
        try {
            cameraRecorder?.stopPreview()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        mCamera?.stopPreview()
        mCamera?.release()
        mCamera = null
    }

    // 停止记录
    fun stopRecord() {
        try {
            cameraRecorder?.stopRecord()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        cameraRecorderState = State.START
    }

    /*-------------------------------------------对焦------------------------------------------------*/
    // 请求对焦
    private fun requestCameraFocus() {
        if (mCamera != null && mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraAutoFocusCallbackHandler.sendEmptyMessageDelayed(1, 1000)
        }
    }

    //设置相机自动对焦
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

    /*-------------------------------------------渲染绘制生命周期-----------------------------------------------*/
    override fun create() {
        try {
            mCamera?.setPreviewTexture(cameraRecorder?.createInputSurfaceTexture())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val mSize = mCamera?.parameters?.previewSize ?: return
        mCameraWidth = mSize.height
        mCameraHeight = mSize.width
        mCamera?.startPreview()
        requestCameraFocus()
        mFilter?.create()
    }

    override fun sizeChanged(width: Int, height: Int) {
        mFilter!!.sizeChanged(width, height)
        MatrixUtils.getMatrix(
                mFilter!!.getVertexMatrix(),
                MatrixUtils.TYPE_CENTERCROP,
                mCameraWidth,
                mCameraHeight,
                width,
                height
        )
        MatrixUtils.flip(mFilter!!.getVertexMatrix(), false, true)
    }

    var a = 0;
    override fun draw(texture: Int) {
        if (a == Int.MAX_VALUE) {
            a = Int.MIN_VALUE
        }
        a++;
        mFilter!!.draw(texture)
    }

    override fun destroy() {
        mFilter!!.destroy()
    }

    /*-------------------------------------------渲染回调-----------------------------------------------*/
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        mCamera = Camera.open(mCurrentCameraId)
        cameraRecorder?.setOutputSurface(Surface(surface))
        cameraRecorder?.setOutputSize(width, height)
        cameraRecorder?.setPreviewSize(width, height)
        cameraRecorder?.setRenderer(this)
        cameraRecorder?.startPreview()
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, width: Int, height: Int) {
        cameraRecorder?.setPreviewSize(width, height)
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        // 停止录制
        if (cameraRecorderState == State.RECORDING) {
            try {
                stopRecord()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        stopPreview()
        return true
    }

    /*-------------------------------------------获取手机的屏幕大小-----------------------------------------------*/


    @SuppressLint("StaticFieldLeak")
    inner class SavePictureTask : AsyncTask<ByteArray?, String?, String?>() {
        override fun doInBackground(vararg params: ByteArray?): String? {
            val fname = DateFormat.format("yyyyMMddhhmmss", Date()).toString() + ".jpg"
            val picture = File(Environment.getExternalStorageDirectory(), fname)
            Log.e("SavePictureTask", "picture >" + picture.absolutePath)
            if (!picture.exists()) {
                picture.createNewFile()
            }
            /**
             * 解决照片旋转90度的问题
             */
            val bitmap = BitmapFactory.decodeByteArray(params[0], 0, params[0]!!.size)
            val matrix = Matrix()
            matrix.postRotate(90f)
            val dstbmp = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height,
                    matrix, true
            )
            var fOut: FileOutputStream? = null
            try {
                fOut = FileOutputStream(picture)
                dstbmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut)
                fOut.flush()
                fOut.close()
            } catch (e: Exception) {
            }
            return null
        }
    }

}