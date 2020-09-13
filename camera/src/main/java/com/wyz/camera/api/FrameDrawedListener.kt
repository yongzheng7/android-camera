package com.wyz.camera.api

import android.opengl.EGLSurface
import com.wyz.camera.core.base.FrameBean

interface FrameDrawedListener{
    /**
     * 渲染完成通知
     * @param surface 渲染的目标EGLSurface
     * @param bean 渲染用的资源
     */
    fun onDrawEnd(surface: EGLSurface?, bean: FrameBean)
}