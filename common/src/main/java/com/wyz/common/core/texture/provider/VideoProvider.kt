package com.wyz.common.core.texture.provider

import android.graphics.Point
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.view.Surface
import com.wyz.common.api.IHardStore
import com.wyz.common.api.ITextureProvider
import com.wyz.common.core.base.HardMediaData
import com.wyz.common.utils.CodecUtil.Companion.getInputBuffer
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Semaphore

class VideoProvider : ITextureProvider {

    private val TIME_OUT = 1000L

    private val LOCK = Object()

    private var mPath: String? = null

    private var mExtractor: MediaExtractor? = null
    private var mVideoDecoder: MediaCodec? = null

    private var mVideoDecodeTrack = -1
    private var mAudioDecodeTrack = -1

    private val mVideoSize = Point()

    private var mFrameSem: Semaphore? = null

    private var mVideoStopTimeStamp: Long = 0

    private var isVideoExtractorEnd = false
    private var isUserWantToStop = false

    private var mDecodeSem: Semaphore? = null

    private var videoProvideEndFlag = false

    private val mStore: IHardStore

    private var nowTimeStamp: Long = -1
    private var mAudioEncodeTrack = -1
    private var mVideoTotalTime: Long = -1

    private val videoDecodeBufferInfo = MediaCodec.BufferInfo()

    constructor(store : IHardStore){
        mStore = store
    }
    fun setInputPath(path: String) {
        mPath = path
    }

    private fun extractMedia(): Boolean {
        if (mPath == null || !File(mPath!!).exists()) {
            //文件不存在
            return false
        }
        try {
            val mMetRet = MediaMetadataRetriever()
            mMetRet.setDataSource(mPath)
            mVideoTotalTime = mMetRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
            mExtractor = MediaExtractor()
            mExtractor!!.setDataSource(mPath!!)
            val trackCount = mExtractor!!.getTrackCount()
            for (i in 0 until trackCount) {
                val format = mExtractor!!.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime!!.startsWith("audio")) {
                    mAudioDecodeTrack = i
                } else if (mime.startsWith("video")) {
                    mVideoDecodeTrack = i
                    var videoRotation = 0
                    val rotation = mMetRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    if (rotation != null) {
                        videoRotation = Integer.valueOf(rotation)
                    }
                    if (videoRotation % 180 != 0) {
                        mVideoSize.y = format.getInteger(MediaFormat.KEY_WIDTH)
                        mVideoSize.x = format.getInteger(MediaFormat.KEY_HEIGHT)
                    } else {
                        mVideoSize.x = format.getInteger(MediaFormat.KEY_WIDTH)
                        mVideoSize.y = format.getInteger(MediaFormat.KEY_HEIGHT)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun videoDecodeStep(): Boolean {
        val mInputIndex = mVideoDecoder!!.dequeueInputBuffer(TIME_OUT)
        if (mInputIndex >= 0) {
            val buffer = getInputBuffer(mVideoDecoder!!, mInputIndex)
            buffer!!.clear()
            synchronized(LOCK) {
                mExtractor!!.selectTrack(mVideoDecodeTrack)
                val ret = mExtractor!!.readSampleData(buffer, 0)
                if (ret != -1) {
                    mVideoStopTimeStamp = mExtractor!!.sampleTime
                    mVideoDecoder!!.queueInputBuffer(mInputIndex, 0, ret, mVideoStopTimeStamp, mExtractor!!.sampleFlags)
                    isVideoExtractorEnd = false
                } else {
                    //可以用!mExtractor.advance，但是貌似会延迟一帧。readSampleData 返回 -1 也表示没有更多数据了
                    isVideoExtractorEnd = true
                }
                mExtractor!!.advance()
            }
        }
        while (true) {
            val mOutputIndex = mVideoDecoder!!.dequeueOutputBuffer(videoDecodeBufferInfo, TIME_OUT)
            if (mOutputIndex >= 0) {
                try {
                    if (!isUserWantToStop) {
                        mDecodeSem!!.acquire()
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                nowTimeStamp = videoDecodeBufferInfo.presentationTimeUs
                mVideoDecoder!!.releaseOutputBuffer(mOutputIndex, true)
                mFrameSem!!.release()
            } else if (mOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            } else if (mOutputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            }
        }
        return isVideoExtractorEnd || isUserWantToStop
    }

    fun getMediaDuration(): Long {
        return mVideoTotalTime
    }

    private fun startDecodeThread() {
        val mDecodeThread = Thread(Runnable {
            while (!videoDecodeStep()) {
            }
            if (videoDecodeBufferInfo.flags != MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                videoProvideEndFlag = true
                //                    try {
//                        mDecodeSem.acquire();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                //释放最后一帧的信号
                videoDecodeBufferInfo.flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM
                mFrameSem!!.release()
            }
            mVideoDecoder!!.stop()
            mVideoDecoder!!.release()
            mVideoDecoder = null
            audioDecodeStep()
            mExtractor!!.release()
            mExtractor = null
            try {
                mStore.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        mDecodeThread.start()
    }

    private val isOpenAudio = true
    private fun audioDecodeStep(): Boolean {
        val buffer = ByteBuffer.allocate(1024 * 64)
        var isTimeEnd = false
        if (isOpenAudio) {
            buffer.clear()
            mExtractor!!.selectTrack(mAudioDecodeTrack)
            val info = MediaCodec.BufferInfo()
            while (true) {
                val length = mExtractor!!.readSampleData(buffer, 0)
                if (length != -1) {
                    val flags = mExtractor!!.sampleFlags
                    val isAudioEnd = mExtractor!!.sampleTime > mVideoStopTimeStamp
                    info.size = length
                    info.flags = if (isAudioEnd) MediaCodec.BUFFER_FLAG_END_OF_STREAM else flags
                    info.presentationTimeUs = mExtractor!!.sampleTime
                    info.offset = 0
                    isTimeEnd = mExtractor!!.sampleTime > mVideoStopTimeStamp
                    mStore.addData(mAudioEncodeTrack, HardMediaData(buffer, info))
                    if (isAudioEnd) {
                        break
                    }
                } else {
                    info.size = 0
                    info.flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    mStore.addData(mAudioEncodeTrack,HardMediaData(buffer, info))
                    isTimeEnd = true
                    break
                }
                mExtractor!!.advance()
            }
        }
        return isTimeEnd
    }

    override fun open(surface: SurfaceTexture): Point {
        try {
            if (!extractMedia()) {
                return Point(0, 0)
            }
            mFrameSem = Semaphore(0)
            mDecodeSem = Semaphore(1)
            videoProvideEndFlag = false
            isUserWantToStop = false
            mAudioEncodeTrack = mStore!!.addTrack(mExtractor!!.getTrackFormat(mAudioDecodeTrack))
            val format = mExtractor!!.getTrackFormat(mVideoDecodeTrack)
            mVideoDecoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            mVideoDecoder!!.configure(format, Surface(surface), null, 0)
            mVideoDecoder!!.start()
            startDecodeThread()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return mVideoSize
    }

    override fun close() {
        isUserWantToStop = true
    }

    override fun frame(): Boolean {
        try {
            mFrameSem!!.acquire()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        mDecodeSem!!.release()
        return videoProvideEndFlag
    }

    override fun getTimeStamp(): Long {
        return nowTimeStamp
    }

    override fun isLandscape(): Boolean {
        return false
    }
}