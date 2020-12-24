package io.horizontalsystems.xrateskit.demo.topmarkets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.xrateskit.demo.R
import io.horizontalsystems.xrateskit.entities.GlobalMarketInfo
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_holder_global_markets.view.*
import java.math.BigDecimal
import java.text.DecimalFormat

class GlobalMarketInfoAdapter: RecyclerView.Adapter<GlobalMarketInfoAdapter.ViewHolderGlobalMarketInfo>(){

    var items = listOf<GlobalMarketInfoItem>()
    lateinit var context : Context

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderGlobalMarketInfo {
        context = parent.context
        return ViewHolderGlobalMarketInfo(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_global_markets, parent, false))
    }

    override fun onBindViewHolder(holder: GlobalMarketInfoAdapter.ViewHolderGlobalMarketInfo, position: Int) {
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
        fun getList(globalMarketInfo: GlobalMarketInfo): List<GlobalMarketInfoItem> {
            val list = mutableListOf<GlobalMarketInfoItem>()
            list.add(GlobalMarketInfoItem("Top Market cap:", globalMarketInfo.marketCap, globalMarketInfo.marketCapDiff24h))
            list.add(GlobalMarketInfoItem("Volume 24h:", globalMarketInfo.volume24h, globalMarketInfo.volume24hDiff24h))
            list.add(GlobalMarketInfoItem("BTC Dominance:", globalMarketInfo.btcDominance, globalMarketInfo.btcDominanceDiff24h))
            list.add(GlobalMarketInfoItem("Defi market cap:", globalMarketInfo.defiMarketCap, globalMarketInfo.defiMarketCapDiff24h))
            list.add(GlobalMarketInfoItem("Defi Tvl:", globalMarketInfo.defiTvl, globalMarketInfo.defiTvlDiff24h))
            return list
        }
    }
}
