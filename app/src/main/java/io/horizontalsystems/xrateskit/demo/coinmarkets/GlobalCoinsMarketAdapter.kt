package io.horizontalsystems.xrateskit.demo.coinmarkets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.xrateskit.demo.R
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarket
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_holder_global_markets.view.*
import java.math.BigDecimal
import java.text.DecimalFormat

class GlobalCoinsMarketAdapter: RecyclerView.Adapter<GlobalCoinsMarketAdapter.ViewHolderGlobalMarketInfo>(){

    var items = listOf<GlobalMarketInfoItem>()
    lateinit var context : Context

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderGlobalMarketInfo {
        context = parent.context
        return ViewHolderGlobalMarketInfo(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_global_markets, parent, false))
    }

    override fun onBindViewHolder(holder: GlobalCoinsMarketAdapter.ViewHolderGlobalMarketInfo, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolderGlobalMarketInfo(override val containerView: View) : RecyclerView.ViewHolder(containerView),
                                                                   LayoutContainer {
        private val txtTitle = containerView.txtGlobalInfoTitle
        private val txtValue = containerView.txtGlobalInfoValue
        private val txtChangeValue = containerView.txtGlobalInfoChange

        fun bind(item: GlobalMarketInfoItem) {
            val dec = DecimalFormat("#,###.00")

            txtTitle.text = item.infoTitle
            txtValue.text = dec.format(item.value)
            txtChangeValue.text = "%.2f".format(item.valueChange)

            if(item.valueChange < BigDecimal.ZERO)
                txtChangeValue.setTextColor(ContextCompat.getColor(context, R.color.red_d))
        }
    }
}

data class GlobalMarketInfoItem(val infoTitle: String, val value: BigDecimal, val valueChange: BigDecimal ){
    companion object {
        fun getList(globalCoinMarket: GlobalCoinMarket): List<GlobalMarketInfoItem> {
            val list = mutableListOf<GlobalMarketInfoItem>()
            list.add(GlobalMarketInfoItem("Top Market cap:", globalCoinMarket.marketCap, globalCoinMarket.marketCapDiff24h))
            list.add(GlobalMarketInfoItem("Volume 24h:", globalCoinMarket.volume24h, globalCoinMarket.volume24hDiff24h))
            list.add(GlobalMarketInfoItem("BTC Dominance:", globalCoinMarket.btcDominance, globalCoinMarket.btcDominanceDiff24h))
            list.add(GlobalMarketInfoItem("Defi market cap:", globalCoinMarket.defiMarketCap, globalCoinMarket.defiMarketCapDiff24h))
            list.add(GlobalMarketInfoItem("Defi Tvl:", globalCoinMarket.defiTvl, globalCoinMarket.defiTvlDiff24h))
            return list
        }
    }
}
