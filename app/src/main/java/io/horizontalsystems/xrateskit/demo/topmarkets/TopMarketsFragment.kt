package io.horizontalsystems.xrateskit.demo.topmarkets

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import io.horizontalsystems.xrateskit.demo.R
import kotlinx.android.synthetic.main.fragment_top_markets.*


class TopMarketsFragment() : Fragment() {

    private val viewModel by viewModels<TopMarketsViewModel> { TopMarketsModule.Factory() }
    private val globalMarketInfoAdapter = GlobalMarketInfoAdapter()
    private val topMarketsAdapter = TopMarketsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_top_markets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLoadMarkets.setOnClickListener {
            viewModel.loadTopMarkets()
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
        }

        btnLoadGLobalMarkets.setOnClickListener {
            viewModel.loadGlobalMarketInfo()
            rviewInfo.visibility = View.GONE
            rviewInfo2.visibility = View.VISIBLE
        }
        btnLoadDefiMarkets.setOnClickListener {
            viewModel.loadTopDefiMarkets()
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
    }
}