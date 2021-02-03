package com.atom.camera.api

interface IObserver<T> {
    fun run(type: T)
}