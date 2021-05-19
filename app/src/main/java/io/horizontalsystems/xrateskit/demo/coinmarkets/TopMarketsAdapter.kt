package io.horizontalsystems.xrateskit.demo.coinmarkets

import android.content.Context
import io.horizontalsystems.xrateskit.entities.CoinMarket
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.xrateskit.demo.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_holder_top_markets.view.*
import java.math.BigDecimal
import java.text.DecimalFormat

class TopMarketsAdapter : RecyclerView.Adapter<TopMarketsAdapter.ViewHolderTopMarkets>() {

    var items = listOf<CoinMarket>()
    lateinit var context: Context

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTopMarkets {
        context = parent.context
        return ViewHolderTopMarkets(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.view_holder_top_markets, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TopMarketsAdapter.ViewHolderTopMarkets, position: Int) {
        holder.bind(items[position], itemCount - position)
    }

    inner class ViewHolderTopMarkets(override val containerView: View) :
        RecyclerView.ViewHolder(containerView),
        LayoutContainer {
        private val txtCoinCode = containerView.txtCoinCode
        private val txtCoinTitle = containerView.txtCoinTitle
        private val txtPrice = containerView.txtPrice
        private val txtIndex = containerView.txtIndex
        private val txtPriceChange = containerView.txtPriceChange

        fun bind(item: CoinMarket, index: Int) {
            containerView.setBackgroundColor(
                if (index % 2 == 0)
                    Color.parseColor("#dddddd") else
                    Color.TRANSPARENT
            )
            val dec = DecimalFormat("#,###.00")
            txtIndex.text = "${itemCount - index + 1}"
            txtCoinCode.text = "${item.data.code}"
            txtCoinTitle.text = "${item.data.title}"
            txtPrice.text = "Volume24h : ${dec.format(item.marketInfo.volume)} - Price:${dec.format(item.marketInfo.rate)} $"

            val rateDiff = item.marketInfo.rateDiffPeriod
            if (rateDiff == null) {
                txtPriceChange.setTextColor(ContextCompat.getColor(context, R.color.grey_50))
                txtPriceChange.text = "----"
            } else if (rateDiff < BigDecimal.ZERO) {
                txtPriceChange.setTextColor(ContextCompat.getColor(context, R.color.red_d))
                txtPriceChange.text = "PriceChange: % " + "%.2f".format(rateDiff)
            }
        }
    }
}
