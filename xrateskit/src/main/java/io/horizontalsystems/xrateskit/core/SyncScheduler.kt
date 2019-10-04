package io.horizontalsystems.xrateskit.core

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SyncScheduler(private val interval: Long, private val retryInterval: Long) : ISyncCompletionListener {

    var listener: Listener? = null

    interface Listener {
        fun onFire()
        fun onStop()
    }

    var disposable: Disposable? = null

    fun start() {
        start(0L)
    }

    fun stop() {
        disposable?.dispose()
        disposable = null
    }

    private fun start(delay: Long) {
        disposable?.dispose()
        disposable = Observable
                .timer(delay, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    listener?.onFire()
                }
    }

    //  Completion Listener

    override fun onSuccess() {
        start(interval)
    }

    override fun onFail() {
        start(retryInterval)
    }
}
