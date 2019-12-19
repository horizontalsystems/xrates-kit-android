package io.horizontalsystems.xrateskit.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.horizontalsystems.xrateskit.XRatesKit
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.entities.MarketInfo
import io.reactivex.Scheduler
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

    private val marketInfoMap = mutableMapOf<String, MarketInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chartStatsRecyclerView.adapter = ratesAdapter

        exchangeRatesKit = XRatesKit.create(this, currency)
        exchangeRatesKit.set(coins)
        coins.forEach { coin ->
            exchangeRatesKit.getMarketInfo(coin, currency)?.let {
                marketInfoMap[coin] = it
            }

            updateMarketInfo()
            observeMarketInfo(coin)
        }

        for (i in 1..10) {
            getHistoricalRate("BTC", (1542105480 + (i * 200)).toLong())
        }

        //get chart points
        val coinCodes = listOf(
            "BTC",
            "ETH",
            "BCH",
            "DASH",
            "BNB",
            "EOS",
            "ZRX",
            "ELF",
            "ANKR",
            "GTO",
            "HOT",
            "BNT",
            "BAT",
            "BUSD",
            "BTCB",
            "CAS",
            "LINK",
            "MCO",
            "CRO",
            "CRPT",
            "DAI",
            "MANA",
            "DGD",
            "DGX",
            "ENJ",
            "MEETONE",
            "NEXO",
            "NDX",
            "NUT",
            "AURA"
        )
        val currency = "USD"
        val chartType = ChartType.DAILY

        coinCodes.forEach { coinCode ->
            observeChartStats(coinCode, currency, chartType)
        }

        unsubscribeBtn.setOnClickListener {
            disposables.dispose()
        }
    }

    private fun getHistoricalRate(coin: String, timestamp: Long) {
        exchangeRatesKit.historicalRateFromApi(coin, currency, timestamp)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                println("historical Rate: $it for TimeStamp: $timestamp")
            }, {
                it.printStackTrace()
            })
            .let {
                disposables.add(it)
            }
    }

    private fun observeMarketInfo(coin: String) {
        exchangeRatesKit.marketInfoObservable(coin, currency)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                marketInfoMap[coin] = it
                updateMarketInfo()
            }, {
                it.printStackTrace()
            })
            .let {
                disposables.add(it)
            }
    }

    private fun updateMarketInfo() {
        latestRate.text = marketInfoMap.map { (coinName, marketInfo) ->
            """
            $coinName
              - Currency: $currency
              - Rate: ${marketInfo.rate}
              - Time: ${Date(marketInfo.timestamp * 1000)}
              - Is Expired: ${marketInfo.isExpired()}
            """.trimIndent()
        }.joinToString("\n\n")
    }

    private fun observeChartStats(
        coinCode: String,
        currency: String,
        chartType: ChartType,
        scheduler: Scheduler = Schedulers.io()
    ) {
        val info = exchangeRatesKit.getChartInfo(coinCode, currency, chartType)
        if (info != null) {
            ratesAdapter.items = info.points
            ratesAdapter.notifyDataSetChanged()
        }

        exchangeRatesKit.chartInfoObservable(coinCode, currency, ChartType.DAILY)
            .subscribeOn(scheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                ratesAdapter.items = it.points
                ratesAdapter.notifyDataSetChanged()
            }, {
                it.printStackTrace()
            })
            .let { disposables.add(it) }
    }
}
