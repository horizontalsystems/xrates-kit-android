package io.horizontalsystems.xrateskit.demo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.xrateskit.entities.ChartPoint
import java.util.*

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
