package io.horizontalsystems.xrateskit.demo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.horizontalsystems.xrateskit.entities.MarketInfo
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_holder_rate.view.*
import java.util.*

class RatesAdapter : RecyclerView.Adapter<RatesAdapter.ViewHolderCoin>() {

    var items = listOf<CoinViewItem>()

    lateinit var presenter: CoinsPresenter

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCoin {
        return ViewHolderCoin(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_rate, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderCoin, position: Int) {
        holder.bind(items[position], itemCount - position)
    }

    inner class ViewHolderCoin(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val coinName = containerView.coinName
        val marketInfo = containerView.marketInfo
        val switchView = containerView.switchView

        fun bind(item: CoinViewItem, index: Int) {
            containerView.setBackgroundColor(if (index % 2 == 0)
                Color.parseColor("#dddddd") else
                Color.TRANSPARENT
            )

            coinName.text = item.coinData.code

            switchView.isChecked = item.isChecked

            containerView.setOnClickListener {
                presenter.onSwitch(item, !switchView.isChecked)
            }

            updateLatestRate(item.latestRate)
        }

        private fun updateLatestRate(info: LatestRate?) {
            if (info == null) {
                marketInfo.text = ""
                return
            }

            val text = """
                Rate: ${info.rate}
                Diff: ${info.rateDiff24h}
                Time: ${Date(info.timestamp * 1000)}
            """.trimIndent()

            marketInfo.text = text
        }
    }
}
