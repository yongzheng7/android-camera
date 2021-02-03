package com.atom.camera.api

import android.media.MediaFormat
import com.atom.camera.core.base.HardMediaData

interface IHardStore : IStore<MediaFormat, HardMediaData> {
    /**
     * 设置存储路径
     * @param path 路径
     */
    fun setOutputPath(path: String)
}