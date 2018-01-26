package com.md.appmanager.threadpool

import android.support.annotation.NonNull
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


/**
 * @author zsmj
 * @date 2018/1/26
 */
class AppManagerThreadPool private constructor() {

    companion object ThreadPool {
        val CORE_POOL_SIZE = 5
        val instance: AppManagerThreadPool by lazy { AppManagerThreadPool() }
    }

    private var scheduledThreadPool: ScheduledExecutorService? = null

    fun startWork(workRunnable: Runnable, delay: Long) {
        if (scheduledThreadPool == null) {
            scheduledThreadPool = ScheduledThreadPoolExecutor(CORE_POOL_SIZE, ThreadFactory { r ->
                val t = Thread(r)
                t.name = "Locker-Thread " + AtomicInteger(0).getAndIncrement()
                t
            })
        }
        scheduledThreadPool!!.schedule(workRunnable, delay, TimeUnit.MILLISECONDS)
    }

    fun startWork(workRunnable: Runnable) {
        startWork(workRunnable, 0)
    }
}