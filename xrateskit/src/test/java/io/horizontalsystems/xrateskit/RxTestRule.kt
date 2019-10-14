package io.horizontalsystems.xrateskit

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers

object RxTestRule {
    //  https://medium.com/@fabioCollini/testing-asynchronous-rxjava-code-using-mockito-8ad831a16877
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
    }
}
