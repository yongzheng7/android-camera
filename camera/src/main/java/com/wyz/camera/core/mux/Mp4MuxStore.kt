package com.wyz.camera.core.mux

import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import com.wyz.camera.api.IHardStore
import com.wyz.camera.core.base.HardMediaData
import java.io.IOException
import java.util.concurrent.*

class Mp4MuxStore : IHardStore {
    private val LOCK = Object()

    private var mMuxer: MediaMuxer? = null
    private val av: Boolean
    private var path: String? = null

    private var audioTrack = -1
    private var videoTrack = -1

    private var muxStarted = false

    private val cache: LinkedBlockingQueue<HardMediaData>
    private val recycler: Recycler<HardMediaData>
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
        Log.e("Mp4MuxStore", "MediaFormat $track")
        var ret = -1
        val s = path ?: return ret
        synchronized(LOCK) {
            if (!muxStarted) {
                if (audioTrack == -1 && videoTrack == -1) {
                    try {
                        mMuxer = MediaMuxer(s, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                mMuxer?.also {
                    val mime = track.getString(MediaFormat.KEY_MIME) ?: return ret
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

    override fun addData(track: Int, data: HardMediaData) {
        if (track >= 0) {
            data.track = track
            if (track == audioTrack || track == videoTrack) {
                var poll = recycler.poll(track)
                if (poll == null) {
                    poll = data.copy()
                } else {
                    data.copyTo(poll)
                }
                while (!cache.offer(poll)) {
                    val c = cache.poll() ?: continue
                    recycler.put(c.track, c)
                }
            }
        }
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
                synchronized(LOCK) {
                    if (muxStarted && data != null) {
                        mMuxer?.writeSampleData(data.track, data.data, data.info)
                        recycler.put(data.track, data)
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
        recycler.clear()
    }

    override fun close() {
        synchronized(LOCK) {
            if (muxStarted) {
                audioTrack = -1
                videoTrack = -1
                muxStarted = false
            }
        }
    }
}