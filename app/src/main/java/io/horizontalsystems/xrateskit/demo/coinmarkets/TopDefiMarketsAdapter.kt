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
import io.horizontalsystems.xrateskit.entities.DefiTvl
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_holder_top_markets.view.*
import java.math.BigDecimal
import java.text.DecimalFormat

class TopDefiMarketsAdapter: RecyclerView.Adapter<TopDefiMarketsAdapter.ViewHolderTopDefiMarkets>(){

    var items = listOf<DefiTvl>()
    lateinit var context : Context

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTopDefiMarkets {
        context = parent.context
        return ViewHolderTopDefiMarkets(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_top_markets, parent, false))
    }

    override fun onBindViewHolder(holderDefi: TopDefiMarketsAdapter.ViewHolderTopDefiMarkets, position: Int) {
        holderDefi.bind(items[position], itemCount - position)
    }

    inner class ViewHolderTopDefiMarkets(override val containerView: View) : RecyclerView.ViewHolder(containerView),
                                                                         LayoutContainer {
        private val txtCoinCode = containerView.txtCoinCode
        private val txtCoinTitle = containerView.txtCoinTitle
        private val txtPrice = containerView.txtPrice
        private val txtIndex = containerView.txtIndex
        private val txtPriceChange = containerView.txtPriceChange

        fun bind(item: DefiTvl, index: Int) {
            containerView.setBackgroundColor(if (index % 2 == 0)
                                                 Color.parseColor("#dddddd") else
                                                 Color.TRANSPARENT
            )
            val dec = DecimalFormat("#,###.00")
            txtIndex.text = "${itemCount - index + 1}"
            txtCoinCode.text = "${item.data.code} (${item.tvlRank})"
            txtCoinTitle.text = "${item.chains?.joinToString(",")}"
            txtPrice.text = "TVL:${dec.format(item.tvl)} $"
            if(item.tvlDiff < BigDecimal.ZERO)
                    txtPriceChange.setTextColor(ContextCompat.getColor(context, R.color.red_d))
            txtPriceChange.text = "TVL Change: % " + "%.2f".format(item.tvlDiff)
        }
    }
}
