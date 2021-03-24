package io.horizontalsystems.xrateskit.coins

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CoinSyncer(
    private val providerCoinsManager: ProviderCoinsManager,
    private val coinInfoManager: CoinInfoManager
) {
    private val disposable = CompositeDisposable()

    fun sync() {
        coinInfoManager.sync()
            .flatMap {
                providerCoinsManager.sync()
            }
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe({
                providerCoinsManager.updatePriorities()
            }, {})
            .let { disposable.add(it) }
    }

}
