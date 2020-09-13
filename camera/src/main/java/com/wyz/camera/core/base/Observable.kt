package com.wyz.camera.core.base

import com.wyz.camera.api.IObservable
import com.wyz.camera.api.IObserver
import java.util.*

open class Observable<T> : IObservable<T> {

    private val observers: ArrayList<IObserver<T>>

    constructor() {
        this.observers = arrayListOf()
    }

    override fun addObserver(observer: IObserver<T>) {
        synchronized(observers){
            observers.add(observer)
        }
    }

    override fun removeObserver(observer: IObserver<T>): Boolean {
        synchronized(observers){
            return observers.remove(observer)
        }
    }

    override fun clearObserver() {
        synchronized(observers){
            observers.clear()
        }
    }

    override fun notify(type: T) {
        synchronized(observers){
            observers.forEach { it.run(type) }
        }
    }
}