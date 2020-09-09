package com.wyz.common.api

import android.graphics.Point
import android.graphics.SurfaceTexture

interface ITextureProvider {
    /**
     * 打开视频流数据源
     */
    fun open(surface: SurfaceTexture): Point

    /**
     * 关闭视频流数据源
     */
    fun close()

    /**
     * 获取一帧数据
     * @return 是否最后一帧
     */
    fun frame(): Boolean

    /**
     * 获取当前帧时间戳
     * @return 时间戳
     */
    fun getTimeStamp(): Long

    /**
     * 视频流是否是横向的
     * @return true or false
     */
    fun isLandscape(): Boolean
}