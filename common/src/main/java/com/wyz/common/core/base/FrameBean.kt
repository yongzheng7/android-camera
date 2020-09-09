package com.wyz.common.core.base

import com.wyz.common.core.egl.EGLHelper

class FrameBean {
    var egl: EGLHelper? = null
    var sourceWidth: Int = 0
    var sourceHeight: Int = 0
    var textureId = 0
    var endFlag = false
    var timeStamp: Long = 0
    var textureTime: Long = 0
    var threadId: Long = 0
}