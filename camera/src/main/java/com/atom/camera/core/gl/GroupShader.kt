package com.atom.camera.core.gl

import android.content.res.Resources
import com.atom.camera.api.Renderer

open class GroupShader : LazyShader {

    private val mGroup: MutableList<Renderer>
    private var isDrawing = false;

    constructor(resource: Resources) : super(resource)

    init {
        mGroup = arrayListOf()
    }

    fun addFilter(filter: Renderer) {
        addFilter(mGroup.size, filter)
    }

    fun addFilter(index: Int, filter: Renderer) {
        if (isDrawing) {
            runOnGLThread(Runnable {
                filter.create()
                filter.sizeChanged(mWidth, mHeight)
                mGroup.add(index, filter)
            })
        } else {
            mGroup.add(index, filter)
        }
    }

    fun removeFilter(index: Int) {
        if(isDrawing){
            runOnGLThread(Runnable {
                val removeAt = mGroup.removeAt(index)
                removeAt.destroy()
            })
        }else{
            mGroup.removeAt(index)
        }

    }

    fun removeFilter(filter: Renderer) {
        if(isDrawing){
            runOnGLThread(Runnable {
                if (mGroup.remove(filter)) {
                    filter.destroy()
                }
            })
        }else{
            mGroup.remove(filter)
        }
    }

    fun get(index: Int): Renderer {
        return mGroup[index]
    }

    fun iterator(): Iterator<Renderer> {
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
        if(!isDrawing){
            isDrawing = true
        }
        var tempTextureId = texture
        mGroup.forEach {
            tempTextureId = it.drawToTexture(tempTextureId)
        }
        super.draw(tempTextureId)
    }
}