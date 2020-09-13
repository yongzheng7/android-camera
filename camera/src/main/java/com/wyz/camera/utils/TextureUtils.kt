package com.wyz.camera.utils

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import javax.microedition.khronos.opengles.GL10

enum class TextureUtils {
    ;
    companion object{
        open fun createTextureID(isOes: Boolean ): Int {
            val target = if (isOes) GLES11Ext.GL_TEXTURE_EXTERNAL_OES else GLES20.GL_TEXTURE_2D
            val texture = IntArray(1)
            GLES20.glGenTextures(1, texture, 0)
            GLES20.glBindTexture(target, texture[0])
            GLES20.glTexParameterf(target, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(target, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
            GLES20.glTexParameteri(target, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(target, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
            GLES20.glBindTexture(target, 0)
            return texture[0]
        }

        open fun createTextureID(target: Int , textureId : Int , bitmap : Bitmap) {
            val oldTexture = IntArray(1) ;
            try{
                GLES20.glGetIntegerv(target, oldTexture, 0)
                GLES20.glBindTexture(target, textureId)
                GLUtils.texImage2D(target, 0, bitmap, 0)
            }finally {
                GLES20.glBindTexture(target, oldTexture[0])
            }
        }

        open fun createTextureID(bitmap : Bitmap): Int {
            val target = GLES20.GL_TEXTURE_2D
            val texture = IntArray(1)
            GLES20.glGenTextures(1, texture, 0)
            GLES20.glBindTexture(target, texture[0])
            GLES20.glTexParameterf(target, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(target, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
            GLES20.glTexParameteri(target, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(target, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            GLES20.glBindTexture(target, 0)
            return texture[0]
        }
    }
}