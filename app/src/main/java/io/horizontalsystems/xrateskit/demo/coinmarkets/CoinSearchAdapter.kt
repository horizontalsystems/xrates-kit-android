package io.horizontalsystems.xrateskit.demo.coinmarkets

import android.content.Context
import io.horizontalsystems.xrateskit.entities.CoinMarket
import android.graphics.Color
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.xrateskit.demo.R
import io.horizontalsystems.xrateskit.entities.CoinData
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_holder_top_markets.view.*
import java.math.BigDecimal
import java.text.DecimalFormat

class CoinSearchAdapter: RecyclerView.Adapter<CoinSearchAdapter.ViewHolderCoinSearch>(){

    var items = listOf<CoinData>()
    lateinit var context : Context

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCoinSearch {
        context = parent.context
        return ViewHolderCoinSearch(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_top_markets, parent, false))
    }

    override fun onBindViewHolder(holder: CoinSearchAdapter.ViewHolderCoinSearch, position: Int) {
        holder.bind(items[position], itemCount - position)
    }

    inner class ViewHolderCoinSearch(override val containerView: View) : RecyclerView.ViewHolder(containerView),
                                                                         LayoutContainer {
        private val txtCoinCode = containerView.txtCoinCode
        private val txtCoinTitle = containerView.txtCoinTitle
        private val txtPrice = containerView.txtPrice
        private val txtIndex = containerView.txtIndex
        private val txtPriceChange = containerView.txtPriceChange

        fun bind(item: CoinData, index: Int) {
            containerView.setBackgroundColor(if (index % 2 == 0)
                                                 Color.parseColor("#dddddd") else
                                                 Color.TRANSPARENT
            )
            txtIndex.text = "${itemCount - index + 1}"
            txtCoinCode.text = "${item.code}"
            txtPrice.text = "${item.title}"
            txtCoinTitle.visibility = View.GONE
            txtPriceChange.visibility = View.GONE
        }
    }
}
