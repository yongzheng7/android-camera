package com.wyz.common.core.gl

import android.content.res.Resources

open class GroupShader : LazyShader {
    private val mGroup: MutableList<BaseShader>

    constructor(resource: Resources) : super(resource)

    constructor() : super()

    init {
        mGroup = arrayListOf()
    }

    fun addFilter(filter: BaseShader) {
        runOnGLThread(Runnable {
            filter.create()
            filter.sizeChanged(mWidth, mHeight)
            mGroup.add(filter)
        })
    }

    fun addFilter(index: Int, filter: BaseShader) {
        runOnGLThread(Runnable {
            filter.create()
            filter.sizeChanged(mWidth, mHeight)
            mGroup.add(index, filter)
        })
    }

    fun removeFilter(index: Int) {
        runOnGLThread(Runnable {
            val removeAt = mGroup.removeAt(index)
            removeAt.destroy()
        })
    }

    fun removeFilter(filter: BaseShader) {
        runOnGLThread(Runnable {
            if(mGroup.remove(filter)){
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

    override fun draw(texture: Int) {
        var tempTextureId = texture
        for (i in mGroup.indices) {
            val filter = mGroup[i]
            tempTextureId = filter.drawToTexture(tempTextureId)
        }
        super.draw(tempTextureId)
    }
}