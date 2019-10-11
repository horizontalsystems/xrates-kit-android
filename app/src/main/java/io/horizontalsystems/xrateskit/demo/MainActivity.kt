package io.horizontalsystems.xrateskit.demo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
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

    private val coins = listOf("BTC")
    private val currency = "USD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val exchangeRatesKit = XRatesKit.create(this, "USD")

        coinsRecyclerView.adapter = ratesAdapter

        exchangeRatesKit.set(coins)
        exchangeRatesKit.set(currency)
        // exchangeRatesKit.getChartStats("BTC", currency, ChartType.DAILY)

        exchangeRatesKit.chartStatsFlowable("BTC", "USD", ChartType.DAILY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ points ->
                    ratesAdapter.items = points
                    ratesAdapter.notifyDataSetChanged()
                }, {
                    it.printStackTrace()
                })
                .let { disposables.add(it) }

        exchangeRatesKit.getHistoricalRate("BTC", currency, 1567932269)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rate ->
                    println(rate)
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
