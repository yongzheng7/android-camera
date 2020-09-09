package com.wyz.common.api

interface IObserver<T> {
    fun run(type: T)
}