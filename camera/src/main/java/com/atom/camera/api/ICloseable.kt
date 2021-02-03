package com.atom.camera.api

import java.lang.Exception

interface ICloseable {
    @Throws(Exception::class)
    fun close()
}