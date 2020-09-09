package com.wyz.common.api

import java.lang.Exception

interface ICloseable {
    @Throws(Exception::class)
    fun close()
}