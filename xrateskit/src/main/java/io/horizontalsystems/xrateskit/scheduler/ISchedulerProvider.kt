package io.horizontalsystems.xrateskit.scheduler

import io.reactivex.Single

interface ISchedulerProvider {

    val id: String
    val lastSyncTimestamp: Long?
    val expirationInterval: Long
    val retryInterval: Long
    val syncSingle: Single<Unit>

    fun notifyExpired()

}
