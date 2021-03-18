package io.horizontalsystems.xrateskit.demo.coinmarkets

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
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.demo.R
import io.horizontalsystems.xrateskit.entities.CoinData
import io.horizontalsystems.xrateskit.entities.TimePeriod
import kotlinx.android.synthetic.main.fragment_top_markets.*


class TopMarketsFragment() : Fragment() {

    private val viewModel by viewModels<TopMarketsViewModel> { TopMarketsModule.Factory() }
    private val globalMarketInfoAdapter = GlobalCoinsMarketAdapter()
    private val topMarketsAdapter = TopMarketsAdapter()
    private val coinMarketDetailsAdapter = CoinsMarketDetailsAdapter()
    private val coinSearchAdapter = CoinSearchAdapter()

    private val coinDatas = listOf(
        CoinData(CoinType.Bitcoin, "BTC", "Bitcoin"),
        CoinData(CoinType.Ethereum, "ETH", "Ethereum",),
        CoinData(CoinType.BitcoinCash, "BCH", "Bch"),
        CoinData(CoinType.Dash, "DASH","Dash"),
        CoinData(CoinType.fromString("bep2|BNB"), "BNB", "Bnb"),
        CoinData(CoinType.fromString("unsupprted|eos"),"EOS", "Eos"),
        CoinData(CoinType.fromString("unsupported|theta-token"),"THETA", "THETA"),
        CoinData(CoinType.Erc20("0x5732046a883704404f284ce41ffadd5b007fd668"),"BLZ", "BLZ"),
        CoinData(CoinType.Erc20("0x1f9840a85d5af5bf1d1762f925bdaddc4201f984"), "UNI", "Uniswap"),
        CoinData(CoinType.Bep20("0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82"),"CAKE", "Cake"))


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

        btnFavorites.setOnClickListener {
            viewModel.loadFavorites(coinDatas.map { it.type }, spAct.timePeriod)
            rviewInfo4.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
            rviewInfo3.visibility = View.GONE
        }

        btnLoadMarkets.setOnClickListener {
            viewModel.loadTopMarkets(spAct.timePeriod)
            rviewInfo4.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
            rviewInfo3.visibility = View.GONE
        }

        btnCategoryDex.setOnClickListener {
            viewModel.loadMarketsByCategory("dexes",spAct.timePeriod)
            rviewInfo4.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
            rviewInfo3.visibility = View.GONE
        }

        btnCategoryBlockchain.setOnClickListener {
            viewModel.loadMarketsByCategory("blockchain",spAct.timePeriod)
            rviewInfo4.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
            rviewInfo3.visibility = View.GONE
        }

        btnLoadGLobalMarkets.setOnClickListener {
            viewModel.loadGlobalMarketInfo()
            rviewInfo4.visibility = View.GONE
            rviewInfo.visibility = View.GONE
            rviewInfo2.visibility = View.VISIBLE
            rviewInfo3.visibility = View.GONE
        }

        btnLoadCoinInfo.setOnClickListener {
            viewModel.loadCoinInfo(CoinType.fromString("erc20|0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"))
            rviewInfo4.visibility = View.GONE
            rviewInfo.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo3.visibility = View.VISIBLE
        }

        btnSearch.setOnClickListener {
            viewModel.searchCoin(eTxtSearch.text.toString())
            rviewInfo3.visibility = View.GONE
            rviewInfo.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo4.visibility = View.VISIBLE
        }

        rviewInfo.adapter = topMarketsAdapter
        rviewInfo2.adapter = globalMarketInfoAdapter
        rviewInfo3.adapter = coinMarketDetailsAdapter
        rviewInfo4.adapter = coinSearchAdapter

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

        viewModel.coinMarketDetails.observe(viewLifecycleOwner, Observer {
            coinMarketDetailsAdapter.items = it
            coinMarketDetailsAdapter.notifyDataSetChanged()
        })

        viewModel.searchCoinsLiveData.observe(viewLifecycleOwner, Observer {
            coinSearchAdapter.items = it
            coinSearchAdapter.notifyDataSetChanged()
        })

        viewModel.progressState.observe(viewLifecycleOwner, Observer {
            if(it)
                progressBar.visibility = View.VISIBLE
            else
                progressBar.visibility = View.GONE
        })
    }
}