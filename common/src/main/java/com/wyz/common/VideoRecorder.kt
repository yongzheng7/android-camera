package com.wyz.common

import android.graphics.Bitmap
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.os.Environment
import android.util.Log
import com.wyz.common.api.FrameDrawedListener
import com.wyz.common.api.IHardStore
import com.wyz.common.api.Renderer
import com.wyz.common.core.audio.SoundRecorder
import com.wyz.common.core.base.FrameBean
import com.wyz.common.core.mux.Mp4MuxStore
import com.wyz.common.core.processor.SurfaceProcessor
import com.wyz.common.core.texture.consume.FrameEncoder
import com.wyz.common.core.texture.consume.FrameShower
import com.wyz.common.core.texture.provider.CameraProvider
import com.wyz.common.core.texture.provider.VideoProvider
import com.wyz.common.utils.DensityUtils
import java.io.*
import java.nio.ByteBuffer
import java.nio.IntBuffer


class VideoRecorder {

    private val mTextureProcessor: SurfaceProcessor

    private val mMuxer: IHardStore

    private val mShower: FrameShower

    private val mSurfaceStore: FrameEncoder

    private val mSoundRecord: SoundRecorder

    constructor(path : String) {
        //用于视频混流和存储
        mMuxer = Mp4MuxStore(true) // 存储消费中进行编码的工具
        //用于预览图像
        mShower = FrameShower() // 展示消费
        mShower.setOutputSize(720, 1280) // 设置输出大小 可以后期调整为自定义获取机器的大小
        //用于编码图像
        mSurfaceStore = FrameEncoder(mMuxer) // 保存消费 视频设置编码工具
        //用于音频
        mSoundRecord = SoundRecorder(mMuxer) // 音频设置编码工具

        val videoProvider = VideoProvider(mMuxer);
        videoProvider.setInputPath(path)
        //用于处理视频图像
        val videoSurfaceProcessor = SurfaceProcessor()
        videoSurfaceProcessor.setTextureProvider(videoProvider) // 摄像头提供者
        videoSurfaceProcessor.addObserver(mShower)
        videoSurfaceProcessor.addObserver(mSurfaceStore)
        mTextureProcessor = videoSurfaceProcessor
    }

    fun setRenderer(renderer: Renderer) {
        mTextureProcessor.setRenderer(renderer)
    }

    fun setSurface(surface: Any) {
        mShower.setSurface(surface)
    }

    fun setOutputPath(path: String) {
        mMuxer.setOutputPath(path)
    }

    fun setPreviewSize(width: Int, height: Int) {
        mShower.setOutputSize(width, height)
    }

    fun open() {
        mTextureProcessor.start()
    }

    fun close() {
        mTextureProcessor.stop()
        stopRecord()
    }

    fun startPreview() {
        mShower.open()
    }

    fun stopPreview() {
        mShower.close()
    }

    @Volatile
    var takePictures: Boolean = false
    fun takePictures() {
        takePictures = true;
    }

    fun startRecord() {
        mSurfaceStore.open()
        mSoundRecord.start()
    }

    fun stopRecord() {
        mSoundRecord.stop()
        mSurfaceStore.close()
        try {
            mMuxer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    interface Listener {
        fun picture(arrays: ByteArray)
    }

}