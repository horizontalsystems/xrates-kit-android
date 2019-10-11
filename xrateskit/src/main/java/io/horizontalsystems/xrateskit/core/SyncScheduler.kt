package io.horizontalsystems.xrateskit.core

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

enum class SyncSchedulerEvent {
    FIRE,
    STOP
}

class SyncScheduler(private val interval: Long, private val retryInterval: Long) : ISyncCompletionListener {
    val eventSubject = PublishSubject.create<SyncSchedulerEvent>()
    var disposable: Disposable? = null

    fun start() {
        start(0L)
    }

    fun stop() {
        disposable?.dispose()
        disposable = null
        eventSubject.onNext(SyncSchedulerEvent.STOP)
    }

    private fun start(delay: Long) {
        disposable?.dispose()
        disposable = Observable
                .timer(delay, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    eventSubject.onNext(SyncSchedulerEvent.FIRE)
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
