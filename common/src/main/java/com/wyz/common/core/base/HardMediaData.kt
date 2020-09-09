package com.wyz.common.core.base

import android.media.MediaCodec
import java.nio.ByteBuffer

class HardMediaData {
    var index = -1
    var data: ByteBuffer
    var info: MediaCodec.BufferInfo

    constructor(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        this.data = buffer
        this.info = bufferInfo
    }

    fun copyTo(data: HardMediaData) : HardMediaData {
        data.index = index
        data.data.position(0)
        data.data.put(this.data)
        data.info.set(info.offset, info.size, info.presentationTimeUs, info.flags)
        return data ;
    }

    fun copy(): HardMediaData {
        val buffer = ByteBuffer.allocate(data.capacity())
        val info = MediaCodec.BufferInfo()
        return copyTo(HardMediaData(buffer, info))
    }
}