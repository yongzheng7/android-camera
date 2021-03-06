package com.atom.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.os.Environment
import com.atom.camera.api.FrameDrawedListener
import com.atom.camera.api.ITextureProvider
import com.atom.camera.api.Renderer
import com.atom.camera.core.base.FrameBean
import com.atom.camera.core.processor.PictureProcessor
import com.atom.camera.core.texture.consume.FrameShower
import com.atom.camera.utils.TextureUtils
import java.io.*
import java.nio.IntBuffer
import java.util.concurrent.Semaphore


class ScreenShower {

    companion object{
        var shower : ScreenShower ?= null
    }

    private val mTextureProcessor: PictureProcessor

    private val mShower: FrameShower

    private var mFrameUpdate: Semaphore? = null

    fun updateFrame(){
        mFrameUpdate?.drainPermits()
        mFrameUpdate?.release()
    }

    constructor(path: String , isAutoUpdate: Boolean) {
        mFrameUpdate = Semaphore(1)
        //用于预览图像
        mShower = FrameShower() // 展示消费
        mShower.setOutputSize(720, 1280) // 设置输出大小 可以后期调整为自定义获取机器的大小
        mShower.setOnDrawEndListener(object : FrameDrawedListener {
            override fun onDrawEnd(surface: EGLSurface?, bean: FrameBean) {
                if (takePictures) {
                    val width = mShower.getWidth()
                    val height = mShower.getHeight()
                    takePictures = false
                    val allocate = IntBuffer.allocate(width * height)
                    GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, allocate)
                    Thread(Runnable {
                        val result = IntArray(width * height)
                        //解决方向颠倒问题
                        //解决方向颠倒问题
                        for (i in 0 until height) {
                            for (j in 0 until width) {
                                val pix: Int = allocate.get(i * width + j)
                                val pb = pix shr 16 and 0xff
                                val pr = pix shl 16 and 0x00ff0000
                                val pix1 = pix and -0xff0100 or pr or pb
                                result[(height - i - 1) * width + j] = pix1
                            }
                        }
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
                        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(result))
                        val file = File(Environment.getExternalStorageDirectory().absolutePath, "new_${System.currentTimeMillis()}.jpeg")
                        if (!file.exists()) {
                            try {
                                file.createNewFile()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                        try {
                            val fos = FileOutputStream(file)
                            val bos = BufferedOutputStream(fos)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                            bos.flush();
                            bos.close();
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                    }).start()
                }
            }
        })
        //用于处理视频图像
        val videoSurfaceProcessor = PictureProcessor()
        videoSurfaceProcessor.setTextureProvider(object : ITextureProvider<IntArray> {
            override fun open(t: IntArray): Point {
                val decodeFile = BitmapFactory.decodeFile(path);
                t[0] = TextureUtils.createTextureID(decodeFile)
                return Point(decodeFile.width, decodeFile.height)
            }

            override fun close() {

            }

            override fun frame(): Boolean {
                if(!isAutoUpdate){
                    try {
                        mFrameUpdate?.acquire()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                return false
            }

            override fun getTimeStamp(): Long {
                return -1
            }

            override fun isLandscape(): Boolean {
                return false
            }
        }) // 摄像头提供者
        videoSurfaceProcessor.addObserver(mShower)
        mTextureProcessor = videoSurfaceProcessor
    }

    fun setRenderer(renderer: Renderer) {
        mTextureProcessor.setRenderer(renderer)
    }


    fun startPreview() {
        mShower.open()
    }

    fun stopPreview() {
        mShower.close()
    }

    fun recyclePreview() {
        mShower.recycle()
    }

    fun isRunning(): Boolean = mTextureProcessor.isRunning()


    fun setSurface(surface: Any) {
        mShower.setSurface(surface)
    }

    fun setPreviewSize(width: Int, height: Int) {
        mShower.setOutputSize(width, height)
    }

    fun open() {
        if(!mTextureProcessor.isRunning()){
            mTextureProcessor.start()
        }
    }

    fun close() {
        if(mTextureProcessor.isRunning()){
            mShower.recycle()
            mTextureProcessor.stop()
        }
    }


    @Volatile
    var takePictures: Boolean = false
    fun takePictures() {
        takePictures = true;
    }

}