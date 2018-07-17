package com.f3401pal.checkabletreeview

import android.os.SystemClock

object IdGenerator {

    private val base by lazy {
        SystemClock.currentThreadTimeMillis()
    }
    private var count = 0

    fun generate(): Long {
        return base + count--
    }
}