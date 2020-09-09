package com.wyz.common.api

interface IStore<Track,Data> : ICloseable {
    /**
     * 增加存储轨道
     * @param track 待存储的内容信息
     * @return 轨道索引
     */
    fun addTrack(track: Track): Int

    /**
     * 写入内容到存储中
     * @param track 轨道索引
     * @param data 存储内容，包括内容信息
     * @return 写入结果
     */
    fun addData(track: Int, data: Data): Int
}