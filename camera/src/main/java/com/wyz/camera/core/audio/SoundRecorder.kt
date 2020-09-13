package com.wyz.camera.core.audio

import android.media.*
import android.os.SystemClock
import com.wyz.camera.api.IHardStore
import com.wyz.camera.core.base.HardMediaData
import com.wyz.camera.core.base.MediaConfig
import com.wyz.camera.utils.CodecUtil
import java.io.IOException
import java.util.concurrent.Executors

class SoundRecorder {
    private var mRecord: AudioRecord? = null

    private var mRecordBufferSize = 0
    private val mRecordSampleRate = 48000 //音频采样率
    private val mRecordChannelConfig = AudioFormat.CHANNEL_IN_STEREO //音频录制通道,默认为立体声
    private val mRecordAudioFormat = AudioFormat.ENCODING_PCM_16BIT //音频录制格式，默认为PCM16Bit

    private var mAudioEncoder: MediaCodec? = null
    private val mConfig = MediaConfig()
    private var isStarted = false
    private var mStore: IHardStore
    private val TIME_OUT = 1000L
    private var mAudioTrack = -1
    private var startTime: Long = 0
    private var stopFlag = false
    private val mExec: Executors? = null

    constructor(store: IHardStore) {
        mStore = store
    }

    fun start() {
        if (!isStarted) {
            stopFlag = false
            mRecordBufferSize = AudioRecord.getMinBufferSize(mRecordSampleRate, mRecordChannelConfig, mRecordAudioFormat) * 2
            mRecord = AudioRecord(MediaRecorder.AudioSource.MIC, mRecordSampleRate, mRecordChannelConfig, mRecordAudioFormat, mRecordBufferSize)
            mRecord?.startRecording()
            try {
                val format = convertAudioConfigToFormat(mConfig.getAudio())
                mAudioEncoder = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME)!!)
                mAudioEncoder?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                mAudioEncoder?.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val thread = Thread(Runnable {
                while (!stopFlag && !audioEncodeStep(false)) {

                }
                audioEncodeStep(true)
                if (isStarted) {
                    mRecord?.stop()
                    mRecord?.release()
                    mRecord = null
                }
                if (mAudioEncoder != null) {
                    mAudioEncoder?.stop()
                    mAudioEncoder?.release()
                    mAudioEncoder = null
                }
                isStarted = false
            })
            thread.start()
            startTime = SystemClock.elapsedRealtimeNanos()
            isStarted = true
        }
    }

    @Synchronized
    private fun audioEncodeStep(isEnd: Boolean): Boolean {
        if (isStarted) {
            val mediaCodec = mAudioEncoder ?: return false
            val inputIndex = mediaCodec.dequeueInputBuffer(TIME_OUT)
            if (inputIndex >= 0) {
                val buffer = CodecUtil.getInputBuffer(mediaCodec, inputIndex)
                buffer?.clear()
                val time = (SystemClock.elapsedRealtimeNanos() - startTime) / 1000
                val length = mRecord!!.read(buffer!!, mRecordBufferSize)
                if (length >= 0) {
                    mediaCodec.queueInputBuffer(inputIndex, 0, length, time, if (isEnd) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0)
                }
            }
            val info = MediaCodec.BufferInfo()
            while (true) {
                val outputIndex = mediaCodec.dequeueOutputBuffer(info, TIME_OUT)
                if (outputIndex >= 0) {
                    val outputBuffer = CodecUtil.getOutputBuffer(mediaCodec, outputIndex)

                    if (outputBuffer != null) {
                        mStore.addData(mAudioTrack, HardMediaData(outputBuffer, info))
                    }
                    mediaCodec.releaseOutputBuffer(outputIndex, false)
                    if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                        stop()
                        return true
                    }
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    mAudioTrack = mStore.addTrack(mediaCodec.outputFormat)
                } else if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                }
            }
        }
        return false
    }

    fun stop() {
        stopFlag = true
    }

    private fun convertAudioConfigToFormat(config: MediaConfig.Audio): MediaFormat {
        val format = MediaFormat.createAudioFormat(config.mime, config.sampleRate, config.channelCount)
        format.setInteger(MediaFormat.KEY_BIT_RATE, config.bitrate)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        return format
    }
}