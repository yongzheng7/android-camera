package com.wyz.common.core.processor

import com.wyz.common.api.IHardStore
import com.wyz.common.core.mux.Mp4MuxStore
import com.wyz.common.core.texture.consume.FrameEncoder
import com.wyz.common.core.texture.consume.FrameShower
import com.wyz.common.core.texture.provider.VideoProvider

class VideoProcessor {

    private val mVideoProvider: VideoProvider

    private var mTextureProcessor: SurfaceProcessor

    private val mShower: FrameShower
    private val mSurfaceStore: FrameEncoder
    private val mMuxer: IHardStore

    constructor() {
        //用于视频混流和存储
        mMuxer = Mp4MuxStore(true)

        //用于预览图像
        mShower = FrameShower()
        mShower.setOutputSize(720, 1280)

        //用于编码图像
        mSurfaceStore = FrameEncoder(mMuxer)

        //用于音频
//        mSoundRecord=new SoundRecorder(mMuxer);
        mVideoProvider = VideoProvider(mMuxer)

        //用于处理视频图像
        mTextureProcessor = SurfaceProcessor()
        mTextureProcessor.setTextureProvider(mVideoProvider)
        mTextureProcessor.addObserver(mShower)
        mTextureProcessor.addObserver(mSurfaceStore)
    }

    fun setSurface(surface: Any) {
        mShower.setSurface(surface)
    }

    fun setInputPath(path: String) {
        mVideoProvider.setInputPath(path)
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
    }

    fun startPreview() {
        mShower.open()
    }

    fun stopPreview() {
        mShower.close()
    }

    fun startRecord() {
        mSurfaceStore.open()
//        mSoundRecord.start();
    }

    fun stopRecord() {
        mSurfaceStore.close()
//        mSoundRecord.stop();
    }
}