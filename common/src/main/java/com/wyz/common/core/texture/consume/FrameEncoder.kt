package com.wyz.common.core.texture.consume

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.opengl.EGLSurface
import android.util.Log
import com.wyz.common.api.FrameDrawedListener
import com.wyz.common.api.IHardStore
import com.wyz.common.core.base.FrameBean
import com.wyz.common.core.base.HardMediaData
import com.wyz.common.core.base.MediaConfig
import com.wyz.common.utils.CodecUtil
import java.io.IOException

class FrameEncoder : FrameShower {

    private var mConfig = MediaConfig()
    private var mVideoEncoder: MediaCodec? = null

    @Volatile
    private var isEncodeStarted = false
    private val TIME_OUT = 1000L

    private val mStore: IHardStore
    private var mVideoTrack = -1

    private var mListener: FrameDrawedListener? = null
    private var startTime: Long = -1

    constructor(store: IHardStore) : super() {
        super.setOnDrawEndListener(object : FrameDrawedListener {
            override fun onDrawEnd(surface: EGLSurface?, bean: FrameBean) {
                if (bean.timeStamp != -1L) {
                    bean.egl?.setPresentationTime(surface, bean.timeStamp * 1000)
                } else {
                    if (startTime == -1L) {
                        startTime = bean.textureTime
                    }
                    bean.egl?.setPresentationTime(surface, bean.textureTime - startTime)
                }
                videoEncodeStep(false)
                Log.e("takePictures", " 1 ")
                mListener?.onDrawEnd(surface, bean)
                Log.e("takePictures", " 10 ")

            }
        })
        this.mStore = store
    }

    override fun run(rb: FrameBean) {
        if (rb.endFlag) {
            videoEncodeStep(true)
        }
        super.run(rb)
    }

    fun setConfig(config: MediaConfig) {
        mConfig = config
    }

    override fun setOutputSize(width: Int, height: Int) {
        super.setOutputSize(width, height)
        mConfig.getVideo().width = width
        mConfig.getVideo().height = height
    }

    protected fun convertVideoConfigToFormat(config: MediaConfig.Video): MediaFormat {
        val format = MediaFormat.createVideoFormat(config.mime, config.width, config.height)
        format.setInteger(MediaFormat.KEY_BIT_RATE, config.bitrate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, config.frameRate)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, config.iframe)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        return format
    }

    private fun openVideoEncoder() {
        if (mVideoEncoder == null) {
            try {
                MediaCodec.createEncoderByType(mConfig.getVideo().mime).also { it ->
                    it.configure(convertVideoConfigToFormat(mConfig.getVideo()), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                    super.setSurface(it.createInputSurface())
                    super.setOutputSize(mConfig.getVideo().width, mConfig.getVideo().height)
                    it.start()
                    isEncodeStarted = true
                    mVideoEncoder = it
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun closeVideoEncoder() {
        mVideoEncoder?.stop()
        mVideoEncoder?.release()
        mVideoEncoder = null
    }

    @Synchronized
    private fun videoEncodeStep(isEnd: Boolean): Boolean {
        if (isEncodeStarted) {
            val mediaCodec = mVideoEncoder ?: return false
            if (isEnd) {
                mediaCodec.signalEndOfInputStream()
            }
            val info = MediaCodec.BufferInfo()
            while (true) {
                val mOutputIndex = mediaCodec.dequeueOutputBuffer(info, TIME_OUT)
                if (mOutputIndex >= 0) {
                    if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        info.size = 0
                    }
                    val outputBuffer = CodecUtil.getOutputBuffer(mediaCodec, mOutputIndex)
                    if (outputBuffer != null) {
                        mStore.addData(mVideoTrack, HardMediaData(outputBuffer, info))
                    }
                    mediaCodec.releaseOutputBuffer(mOutputIndex, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        closeVideoEncoder()
                        isEncodeStarted = false
                        break
                    }
                } else if (mOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    mVideoTrack = mStore.addTrack(mediaCodec.outputFormat)
                } else if (mOutputIndex == MediaCodec.INFO_TRY_AGAIN_LATER && !isEnd) {
                    break
                }
            }
        }
        return false
    }

    override fun open() {
        openVideoEncoder()
        super.open()
    }


    override fun close() {
        super.close()
        videoEncodeStep(true)
        startTime = -1
    }

    override fun setOnDrawEndListener(listener: FrameDrawedListener) {
        this.mListener = listener
    }

    override fun setSurface(surface: Any?) {}
}