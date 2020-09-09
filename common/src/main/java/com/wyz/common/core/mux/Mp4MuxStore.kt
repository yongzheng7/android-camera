package com.wyz.common.core.mux

import android.media.MediaFormat
import com.wyz.common.api.IHardStore
import com.wyz.common.core.base.HardMediaData

class Mp4MuxStore : IHardStore {
    override fun setOutputPath(path: String) {
        TODO("Not yet implemented")
    }

    override fun addTrack(track: MediaFormat): Int {
        TODO("Not yet implemented")
    }

    override fun addData(track: Int, data: HardMediaData): Int {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}