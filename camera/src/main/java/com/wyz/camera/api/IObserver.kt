package com.wyz.camera.api

interface IObserver<T> {
    fun run(type: T)
}