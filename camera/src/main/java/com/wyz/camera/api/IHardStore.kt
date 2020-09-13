package com.wyz.camera.api

import android.media.MediaFormat
import com.wyz.camera.core.base.HardMediaData

interface IHardStore : IStore<MediaFormat, HardMediaData> {
    /**
     * 设置存储路径
     * @param path 路径
     */
    fun setOutputPath(path: String)
}