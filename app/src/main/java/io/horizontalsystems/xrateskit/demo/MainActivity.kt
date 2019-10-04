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
import io.horizontalsystems.xrateskit.storage.Rate
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val ratesAdapter = RatesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var rates = mutableListOf<Rate>()
        val exchangeRatesKit = XRatesKit.create(this)

        coinsRecyclerView.adapter = ratesAdapter

        exchangeRatesKit.start(listOf("BTC", "ETH", "BCH"), "USD")
        exchangeRatesKit.rateFlowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rate ->
                    rates = rates.filter { it.coin != rate.coin }.toMutableList()
                    rates.add(rate)

                    ratesAdapter.items = rates
                    ratesAdapter.notifyDataSetChanged()
                }, {
                    it.printStackTrace()
                })
                .let {

                }
    }
}

class RatesAdapter : RecyclerView.Adapter<ViewHolderTransaction>() {
    var items = listOf<Rate>()

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

    fun bind(rate: Rate, index: Int) {
        containerView.setBackgroundColor(if (index % 2 == 0)
            Color.parseColor("#dddddd") else
            Color.TRANSPARENT
        )

        val value = """
            - #$index
            - Coin: ${rate.coin}
            - Rate: ${rate.value}
            - Time: ${Date(rate.timestamp * 1000)}
        """

        summary.text = value.trimIndent()
    }
}
