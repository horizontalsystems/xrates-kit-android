package io.horizontalsystems.xrateskit.demo.coinmarkets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.xrateskit.demo.R
import io.horizontalsystems.xrateskit.entities.CoinMarketDetails
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_holder_global_markets.view.*
import java.math.BigDecimal
import java.text.DecimalFormat

class CoinsMarketDetailsAdapter: RecyclerView.Adapter<CoinsMarketDetailsAdapter.ViewHolderCoinMarketDetails>(){

    var items = listOf<CoinMarketDetailsItem>()
    lateinit var context : Context

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCoinMarketDetails {
        context = parent.context
        return ViewHolderCoinMarketDetails(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_global_markets, parent, false))
    }

    override fun onBindViewHolder(holder: CoinsMarketDetailsAdapter.ViewHolderCoinMarketDetails, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolderCoinMarketDetails(override val containerView: View) : RecyclerView.ViewHolder(containerView),
                                                                   LayoutContainer {
        private val txtTitle = containerView.txtGlobalInfoTitle
        private val txtValue = containerView.txtGlobalInfoValue
        private val txtChangeValue = containerView.txtGlobalInfoChange

        fun bind(item: CoinMarketDetailsItem) {
            val dec = DecimalFormat("#,###.00")

            txtTitle.text = item.infoTitle
            if(item.value.compareTo(BigDecimal.ZERO) != 0)
                txtValue.text = dec.format(item.value)
            else
                txtValue.text = ""

            if(item.valueChange.compareTo(BigDecimal.ZERO) != 0 ){
                txtChangeValue.text = "%.2f".format(item.valueChange)
                if(item.valueChange < BigDecimal.ZERO)
                    txtChangeValue.setTextColor(ContextCompat.getColor(context, R.color.red_d))
            } else {
                txtChangeValue.text = ""
            }
        }
    }
}

data class CoinMarketDetailsItem(val infoTitle: String, val value: BigDecimal, val valueChange: BigDecimal ){
    companion object {
        fun getList(coinMarketDetails: CoinMarketDetails): List<CoinMarketDetailsItem> {
            val list = mutableListOf<CoinMarketDetailsItem>()
            list.add(CoinMarketDetailsItem("Coin/Price :${coinMarketDetails.data.code}:", coinMarketDetails.rate, BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("Price 24h High/Low :", coinMarketDetails.rateHigh24h, coinMarketDetails.rateLow24h))
            list.add(CoinMarketDetailsItem("Volume 24h:", coinMarketDetails.volume24h, BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("MarketCap:", coinMarketDetails.marketCap, coinMarketDetails.marketCapDiff24h ?: BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("DefiTvl:", coinMarketDetails.defiTvlInfo?.marketCapTvlRatio?: BigDecimal.ZERO, BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("--------------- Diff ------------------", BigDecimal.ZERO, BigDecimal.ZERO))
            coinMarketDetails.rateDiffs.forEach { periodDiff ->
                list.add(CoinMarketDetailsItem("TimePeriod:${periodDiff.key}", BigDecimal.ZERO, BigDecimal.ZERO))
                periodDiff.value.forEach{ coinDiff ->
                    list.add(CoinMarketDetailsItem("${coinDiff.key} :", coinDiff.value ,BigDecimal.ZERO))

                }
            }
            list.add(CoinMarketDetailsItem("--------------- Rating ------------------", BigDecimal.ZERO, BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("${coinMarketDetails.meta.rating}", BigDecimal.ZERO, BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("--------------- Security ------------------", BigDecimal.ZERO, BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("IsDecentralized : ${coinMarketDetails.meta.securityParameter?.decentralized}", BigDecimal.ZERO, BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("Privay : ${coinMarketDetails.meta.securityParameter?.privacy}", BigDecimal.ZERO, BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("CensorshipResistance : ${coinMarketDetails.meta.securityParameter?.censorshipResistance}", BigDecimal.ZERO, BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("--------------- Categories ------------------", BigDecimal.ZERO, BigDecimal.ZERO))
            list.add(CoinMarketDetailsItem("${coinMarketDetails.meta.categories?.joinToString(",")}", BigDecimal.ZERO, BigDecimal.ZERO))
            coinMarketDetails.meta.platforms?.let {
                if(!it.isEmpty()){
                    list.add(CoinMarketDetailsItem("--------------- Platform ------------------", BigDecimal.ZERO, BigDecimal.ZERO))
                    it.forEach {
                        list.add(CoinMarketDetailsItem("${it.key} - ${it.value}", BigDecimal.ZERO, BigDecimal.ZERO))
                    }
                }
            }

            list.add(CoinMarketDetailsItem("--------------- Info ------------------", BigDecimal.ZERO, BigDecimal.ZERO))
            coinMarketDetails.meta.links.forEach{
                list.add(CoinMarketDetailsItem("${ it.key } : ${it.value}", BigDecimal.ZERO, BigDecimal.ZERO))
            }

            if(!coinMarketDetails.tickers.isEmpty()){
                list.add(CoinMarketDetailsItem("--------------- Tickers ------------------", BigDecimal.ZERO, BigDecimal.ZERO))
                coinMarketDetails.tickers.forEach {
                    list.add(CoinMarketDetailsItem("${it.base} / ${it.target} - ${it.marketName}", it.rate, it.volume))
                    list.add(CoinMarketDetailsItem("${it.imageUrl}", BigDecimal.ZERO, BigDecimal.ZERO))
                }
            }

            list.add(CoinMarketDetailsItem("--------------- Funds ------------------", BigDecimal.ZERO, BigDecimal.ZERO))
            coinMarketDetails.meta.fundCategories.forEach{
                list.add(CoinMarketDetailsItem("Category : ${ it.name } : ${it.order}", BigDecimal.ZERO, BigDecimal.ZERO))
                it.funds.forEach{
                    list.add(CoinMarketDetailsItem("${ it.name }", BigDecimal.ZERO, BigDecimal.ZERO))
                    list.add(CoinMarketDetailsItem("${ it.url }", BigDecimal.ZERO, BigDecimal.ZERO))
                    list.add(CoinMarketDetailsItem("--------", BigDecimal.ZERO, BigDecimal.ZERO))
                }
            }

            list.add(CoinMarketDetailsItem("--------------- Treasures ------------------", BigDecimal.ZERO, BigDecimal.ZERO))
            coinMarketDetails.treasuries?.forEach{
                list.add(CoinMarketDetailsItem("Company : ${ it.company.name } : ${it.amount}", BigDecimal.ZERO, BigDecimal.ZERO))
            }

            return list
        }
    }
}
