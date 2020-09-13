package com.wyz.camera.api

interface Renderer {

    /**
     * 创建
     */
    fun create()

    /**
     * 大小改变
     */
    fun sizeChanged(width: Int, height: Int)

    /**
     * 渲染
     * @param texture 输入纹理
     */
    fun draw(texture: Int)

    /**
     * 销毁
     */
    fun destroy()

}