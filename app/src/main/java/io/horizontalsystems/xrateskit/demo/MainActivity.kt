package io.horizontalsystems.xrateskit.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.horizontalsystems.xrateskit.XRatesKit
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.entities.RateInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var exchangeRatesKit: XRatesKit

    private val ratesAdapter = RatesAdapter()
    private val disposables = CompositeDisposable()

    private val coins = listOf("BTC")
    private val currency = "USD"

    private val latestRates = mutableMapOf<String, RateInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chartStatsRecyclerView.adapter = ratesAdapter

        exchangeRatesKit = XRatesKit.create(this, currency)
        exchangeRatesKit.set(coins)
        coins.forEach { coin ->
            exchangeRatesKit.getLatestRate(coin, currency)?.let {
                latestRates[coin] = it
            }

            updateLatestRates()
            observeLatestRates(coin)
        }

        observeChartStats("BTC")

        unsubscribeBtn.setOnClickListener {
            disposables.dispose()
        }
    }

    private fun observeLatestRates(coin: String) {
        exchangeRatesKit.latestRateObservable(coin, currency)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    latestRates[coin] = it
                    updateLatestRates()
                }, {
                    it.printStackTrace()
                })
                .let {
                    disposables.add(it)
                }
    }

    private fun updateLatestRates() {
        latestRate.text = latestRates.map { (coinName, rate) ->
            """
            $coinName
              - Currency: $currency
              - Rate: ${rate.value}
              - Time: ${Date(rate.timestamp * 1000)}
              - Is Expired: ${rate.isExpired()}
            """.trimIndent()
        }.joinToString("\n\n")
    }

    private fun observeChartStats(coin: String) {
        ratesAdapter.items = exchangeRatesKit.getChartPoints(coin, currency, ChartType.DAILY)
        ratesAdapter.notifyDataSetChanged()

        exchangeRatesKit.chartPointsObservable(coin, currency, ChartType.DAILY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    ratesAdapter.items = it
                    ratesAdapter.notifyDataSetChanged()
                }, {
                    it.printStackTrace()
                })
                .let { disposables.add(it) }
    }
}
