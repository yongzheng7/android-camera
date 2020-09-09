package com.wyz.common.core.mux

import android.media.MediaFormat
import android.media.MediaMuxer
import com.wyz.common.api.IHardStore
import com.wyz.common.core.base.HardMediaData
import java.io.IOException
import java.util.concurrent.*

class StrengthenMp4MuxStore : IHardStore {
    private var mMuxer: MediaMuxer? = null
    private val av : Boolean
    private var path: String? = null

    private var audioTrack = -1
    private var videoTrack = -1

    private val Lock = Object()

    private var muxStarted = false

    private val cache: LinkedBlockingQueue<HardMediaData>
    private var recycler: Recycler<HardMediaData>? = null
    private var exec: ExecutorService? = null

    constructor(av: Boolean) {
        this.av = av
        cache = LinkedBlockingQueue(30)
        recycler = Recycler()
        exec = ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, LinkedBlockingQueue(16), Executors.defaultThreadFactory())
    }

    override fun setOutputPath(path: String) {
        this.path = path
    }

    override fun addTrack(track: MediaFormat): Int {
        var ret = -1
        val s = path ?: return ret
        synchronized(Lock) {
            if (!muxStarted) {
                if (audioTrack == -1 && videoTrack == -1) {
                    try {
                        mMuxer = MediaMuxer(s, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                mMuxer?.also {
                    val mime= track.getString(MediaFormat.KEY_MIME) ?:return ret
                    if (mime.startsWith("audio")) {
                        audioTrack = it.addTrack(track)
                        ret = audioTrack
                    } else if (mime.startsWith("video")) {
                        videoTrack = it.addTrack(track)
                        ret = videoTrack
                    }
                    startMux()
                }
            }
        }
        return ret
    }

    override fun addData(track: Int, data: HardMediaData): Int {
        if (track >= 0) {
            data.index = track
            if (track == audioTrack || track == videoTrack) {
                var d = recycler?.poll(track)
                if (d == null) {
                    d = data.copy()
                } else {
                    data.copyTo(d)
                }
                while (!cache.offer(d)) {
                    val c = cache.poll() ?:continue
                    recycler?.put(c.index, c)
                }
            }
        }
        return 0
    }

    private fun startMux() {
        val canMux = !av || (audioTrack != -1 && videoTrack != -1)
        if (canMux) {
            mMuxer?.start()
            muxStarted = true
            exec?.execute { muxRun() }
        }
    }

    private fun muxRun() {
        while (muxStarted) {
            try {
                val data = cache.poll(1, TimeUnit.SECONDS)
                synchronized(Lock) {
                    if (muxStarted && data != null) {
                        mMuxer?.writeSampleData(data.index, data.data, data.info)
                        recycler?.put(data.index, data)
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        try {
            mMuxer?.stop()
            mMuxer?.release()
            mMuxer = null
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        cache.clear()
        recycler?.clear()
    }

    override fun close() {
        synchronized(Lock) {
            if (muxStarted) {
                audioTrack = -1
                videoTrack = -1
                muxStarted = false
            }
        }
    }
}