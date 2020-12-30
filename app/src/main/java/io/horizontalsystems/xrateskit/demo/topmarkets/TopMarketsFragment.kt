package io.horizontalsystems.xrateskit.demo.topmarkets

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.horizontalsystems.xrateskit.demo.R
import io.horizontalsystems.xrateskit.entities.TimePeriod
import kotlinx.android.synthetic.main.fragment_top_markets.*


class TopMarketsFragment() : Fragment() {

    private val viewModel by viewModels<TopMarketsViewModel> { TopMarketsModule.Factory() }
    private val globalMarketInfoAdapter = GlobalMarketInfoAdapter()
    private val topMarketsAdapter = TopMarketsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_top_markets, container, false)
    }

    class SpinnerActivity(var timePeriod: TimePeriod = TimePeriod.HOUR_24) : Activity(), AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            timePeriod = if(pos == 0)
                TimePeriod.HOUR_24
            else if(pos == 1)
                TimePeriod.DAY_7
            else
                TimePeriod.DAY_30
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Another interface callback
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spAct = SpinnerActivity()

        ArrayAdapter.createFromResource(
                this.requireContext(),
                R.array.periods,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spPeriod.adapter = adapter
            spPeriod.onItemSelectedListener = spAct
        }

        btnLoadMarkets.setOnClickListener {
            viewModel.loadTopMarkets(spAct.timePeriod)
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
        }

        btnLoadGLobalMarkets.setOnClickListener {
            viewModel.loadGlobalMarketInfo()
            rviewInfo.visibility = View.GONE
            rviewInfo2.visibility = View.VISIBLE
        }
        btnLoadDefiMarkets.setOnClickListener {
            viewModel.loadTopDefiMarkets(spAct.timePeriod)
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
        }

        rviewInfo.adapter = topMarketsAdapter
        rviewInfo2.adapter = globalMarketInfoAdapter
        //ConcatAdapter(globalMarketInfoAdapter, topMarketsAdapter)

        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.topMarkets.observe(viewLifecycleOwner, Observer {
            topMarketsAdapter.items = it
            topMarketsAdapter.notifyDataSetChanged()
        })

        viewModel.globalMarketInfo.observe(viewLifecycleOwner, Observer {
            globalMarketInfoAdapter.items = it
            globalMarketInfoAdapter.notifyDataSetChanged()
        })

        viewModel.progressState.observe(viewLifecycleOwner, Observer {
            if(it)
                progressBar.visibility = View.VISIBLE
            else
                progressBar.visibility = View.GONE
        })
    }
}