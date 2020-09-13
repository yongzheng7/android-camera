package com.wyz.camera.api

interface IObservable<T> {

    fun addObserver(observer: IObserver<T>)

    fun removeObserver(observer: IObserver<T>): Boolean

    fun clearObserver()

    fun notify(type: T)

}