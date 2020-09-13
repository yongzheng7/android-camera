package com.wyz.camera.core.gl

import android.content.res.Resources

open class GroupShader : LazyShader {
    private val mGroup: MutableList<BaseShader>

    constructor(resource: Resources) : super(resource)

    init {
        mGroup = arrayListOf()
    }

    fun addFilter(filter: BaseShader) {
        mGroup.add(filter)
    }

    fun addFilter(index: Int, filter: BaseShader) {
        mGroup.add(index, filter)
    }

    fun removeFilter(index: Int) {
        runOnGLThread(Runnable {
            val removeAt = mGroup.removeAt(index)
            removeAt.destroy()
        })
    }

    fun removeFilter(filter: BaseShader) {
        runOnGLThread(Runnable {
            if (mGroup.remove(filter)) {
                filter.destroy()
            }
        })
    }

    fun get(index: Int): BaseShader {
        return mGroup[index]
    }

    fun iterator(): Iterator<BaseShader> {
        return mGroup.iterator()
    }

    fun isEmpty(): Boolean {
        return mGroup.isEmpty()
    }

    override fun create() {
        super.create()
        mGroup.forEach {
            it.create()
        }
    }

    override fun sizeChanged(width: Int, height: Int) {
        super.sizeChanged(width, height)
        mGroup.forEach {
            it.sizeChanged(width, height)
        }
    }

    override fun draw(texture: Int) {
        var tempTextureId = texture
        mGroup.forEach {
            tempTextureId = it.drawToTexture(tempTextureId)
        }
        super.draw(tempTextureId)
    }
}