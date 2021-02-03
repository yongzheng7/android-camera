package com.atom.camera.core.gl.tools

import com.atom.camera.core.gl.BaseShader

class ShaderTools {
    private var threadId: Long = -1
    private val filters: MutableMap<Class<*>, BaseShader?> = mutableMapOf()

    /**
     * 处理一个纹理，并输出处理后的纹理
     * @param texture 输入纹理
     * @param width 输出纹理宽度
     * @param height 输出纹理高度
     * @param clazz 滤镜类型
     * @return 输出纹理
     */
    fun processTexture(texture: Int, width: Int, height: Int, clazz: Class<out BaseShader?>): Int {
        val nowThreadId = Thread.currentThread().id
        if (nowThreadId != threadId) {
            filters.clear()
            threadId = nowThreadId
        }
        var filter = filters[clazz]
        if (filter == null) {
            try {
                filter = clazz.newInstance()
                filter?.create()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            filters[clazz] = filter
        }
        if (filter != null) {
            filter.sizeChanged(width, height)
            return filter.drawToTexture(texture)
        }
        return -1
    }

    /**
     * 处理一个纹理，并输出处理后的纹理
     * @param texture 输入纹理
     * @param width 输出纹理宽度
     * @param height 输出纹理高度
     * @param filter 滤镜实体
     * @return 输出纹理
     */
    fun processTexture(texture: Int, width: Int, height: Int, filter: BaseShader?): Int {
        val nowThreadId = Thread.currentThread().id
        if (nowThreadId != threadId) {
            filters.clear()
            threadId = nowThreadId
        }
        if (filter != null) {
            val clazz: Class<*> = filter::class.java
            if (filters[clazz] == null) {
                filters[clazz] = filter
                filter.create()
            }
            filter.sizeChanged(width, height)
            return filter.drawToTexture(texture)
        }
        return -1
    }

    fun onGlDestroy() {
        for ((_, filter) in filters) {
            filter?.destroy()
        }
        filters.clear()
    }
}