package com.wyz.common.utils

import android.opengl.GLES11Ext
import android.opengl.GLES20
import javax.microedition.khronos.opengles.GL10

enum class TextureUtils {
    ;
    companion object{
        open fun createTextureID(isOes: Boolean): Int {
            val target = if (isOes) GLES11Ext.GL_TEXTURE_EXTERNAL_OES else GLES20.GL_TEXTURE_2D
            val texture = IntArray(1)
            GLES20.glGenTextures(1, texture, 0)
            GLES20.glBindTexture(target, texture[0])
            GLES20.glTexParameterf(target, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(target, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
            GLES20.glTexParameteri(target, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(target, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
            return texture[0]
        }
    }
}