package com.wyz.common.core.mux

import android.util.SparseArray
import java.util.concurrent.LinkedBlockingQueue

class Recycler<T> {

    private val datas = SparseArray<LinkedBlockingQueue<T>>()

    fun put(index: Int, t: T) {
        if (datas.indexOfKey(index) < 0) {
            datas.append(index, LinkedBlockingQueue())
        }
        datas[index].add(t)
    }

    fun poll(index: Int): T? {
        return if (datas.indexOfKey(index) >= 0) {
            datas[index].poll()
        } else null
    }

    fun clear() {
        datas.clear()
    }
}