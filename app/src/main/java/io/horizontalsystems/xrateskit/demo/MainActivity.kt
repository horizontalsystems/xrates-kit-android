package io.horizontalsystems.xrateskit.demo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.facebook.stetho.Stetho
import io.horizontalsystems.xrateskit.XRatesKit
import io.horizontalsystems.xrateskit.entities.ChartPoint
import io.horizontalsystems.xrateskit.entities.ChartType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val ratesAdapter = RatesAdapter()
    private val disposables = CompositeDisposable()

    private val btc = "BTC"
    private val coins = listOf(btc)
    private val currency = "USD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val exchangeRatesKit = XRatesKit.create(this, currency)

        chartStatsRecyclerView.adapter = ratesAdapter

        exchangeRatesKit.set(coins)
        // exchangeRatesKit.getChartStats("BTC", currency, ChartType.DAILY)

        exchangeRatesKit.latestRateFlowable(btc, currency)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rate ->
                    val value = """
                        - Coin: ${rate.coin}
                        - Currency: ${rate.currency}
                        - Rate: ${rate.value}
                        - Time: ${Date(rate.timestamp * 1000)}
                    """

                    latestRate.text = value.trimIndent()
                }, {
                    it.printStackTrace()
                })
                .let { disposables.add(it) }

        ratesAdapter.items = exchangeRatesKit.getChartStats(btc, currency, ChartType.DAILY)
        ratesAdapter.notifyDataSetChanged()

        exchangeRatesKit.chartStatsFlowable(btc, currency, ChartType.DAILY)
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

class RatesAdapter : RecyclerView.Adapter<ViewHolderTransaction>() {
    var items = listOf<ChartPoint>()

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTransaction {
        return ViewHolderTransaction(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_rate, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderTransaction, position: Int) {
        holder.bind(items[position], itemCount - position)
    }
}

class ViewHolderTransaction(private val containerView: View) : RecyclerView.ViewHolder(containerView) {
    private val summary = containerView.findViewById<TextView>(R.id.summary)!!

    fun bind(rate: ChartPoint, index: Int) {
        containerView.setBackgroundColor(if (index % 2 == 0)
            Color.parseColor("#dddddd") else
            Color.TRANSPARENT
        )

        val value = """
          - #$index
          - Rate: ${rate.value}
          - Time: ${Date(rate.timestamp * 1000)}
        """

        summary.text = value.trimIndent()
    }
}
