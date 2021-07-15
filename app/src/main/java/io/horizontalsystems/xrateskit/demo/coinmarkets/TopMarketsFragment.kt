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
import com.google.android.material.snackbar.Snackbar
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.demo.R
import io.horizontalsystems.xrateskit.entities.CoinData
import io.horizontalsystems.xrateskit.entities.TimePeriod
import kotlinx.android.synthetic.main.fragment_top_markets.*


class TopMarketsFragment() : Fragment() {

    private val viewModel by viewModels<TopMarketsViewModel> { TopMarketsModule.Factory() }
    private val globalMarketInfoAdapter = GlobalCoinsMarketAdapter()
    private val topMarketsAdapter = TopMarketsAdapter()
    private val topDefiMarketsAdapter = TopDefiMarketsAdapter()
    private val coinMarketDetailsAdapter = CoinsMarketDetailsAdapter()
    private val coinSearchAdapter = CoinSearchAdapter()

    private val coinDatas = listOf(
        CoinData(CoinType.Bitcoin, "BTC", "Bitcoin"),
        CoinData(CoinType.Ethereum, "ETH", "Ethereum"),
        CoinData(CoinType.BitcoinCash, "BCH", "Bch"),
        CoinData(CoinType.Dash, "DASH","Dash"),
        CoinData(CoinType.fromString("bep2|BNB"), "BNB", "Bnb"),
        CoinData(CoinType.fromString("unsupprted|eos"),"EOS", "Eos"),
        CoinData(CoinType.fromString("unsupported|theta-token"),"THETA", "THETA"),
        CoinData(CoinType.Erc20("0x5732046a883704404f284ce41ffadd5b007fd668"),"BLZ", "BLZ"),
        CoinData(CoinType.Erc20("0x1f9840a85d5af5bf1d1762f925bdaddc4201f984"), "UNI", "Uniswap"),
        CoinData(CoinType.Bep20("0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82"),"CAKE", "Cake"),
        CoinData(CoinType.Erc20("0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2"),"Maker", "MKR"))


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
            rviewInfoDefi.visibility = View.GONE
        }

        btnLoadNews.setOnClickListener {
            viewModel.loadNews()
        }

        btnLoadMarkets.setOnClickListener {
            viewModel.loadTopMarkets(spAct.timePeriod)
            rviewInfo4.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
            rviewInfo3.visibility = View.GONE
            rviewInfoDefi.visibility = View.GONE
        }

        btnTopDefi.setOnClickListener {
            viewModel.loadTopDefiMarkets()
            rviewInfoDefi.visibility = View.VISIBLE
            rviewInfo4.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.GONE
            rviewInfo3.visibility = View.GONE
        }

        btnCategoryDex.setOnClickListener {
            viewModel.loadMarketsByCategory("dexes",spAct.timePeriod)
            rviewInfo4.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
            rviewInfo3.visibility = View.GONE
            rviewInfoDefi.visibility = View.GONE
        }

        btnTVL.setOnClickListener {
            //val coinType = CoinType.fromString("erc20|0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9") //aave
            //val coinType = CoinType.fromString("unsupported|theta-token") //Theta
            val coinType = CoinType.fromString("erc20|0x111111111117dc0aa78b770fa6a738034120c302") //1inch
            //viewModel.loadTvl(coinType)
            viewModel.loadAuditInfo(coinType)
        }

        btnLoadGLobalMarkets.setOnClickListener {
            viewModel.loadGlobalMarketInfo()
            rviewInfo4.visibility = View.GONE
            rviewInfo.visibility = View.GONE
            rviewInfo2.visibility = View.VISIBLE
            rviewInfo3.visibility = View.GONE
            rviewInfoDefi.visibility = View.GONE
        }

        btnLoadCoinInfo.setOnClickListener {
            //val coinType = CoinType.fromString("bitcoin")
            //val coinType = CoinType.fromString("erc20|0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2") //Maker
            val coinType = CoinType.fromString("erc20|0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9") //aave
            //val coinType = CoinType.fromString("unsupported|theta-token") //Theta
            viewModel.loadCoinInfo(coinType)
            rviewInfo4.visibility = View.GONE
            rviewInfo.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo3.visibility = View.VISIBLE
            rviewInfoDefi.visibility = View.GONE
        }

        btnSearch.setOnClickListener {
            viewModel.searchCoin(eTxtSearch.text.toString())
            rviewInfo3.visibility = View.GONE
            rviewInfo.visibility = View.GONE
            rviewInfo2.visibility = View.GONE
            rviewInfo4.visibility = View.VISIBLE
            rviewInfoDefi.visibility = View.GONE
        }

        rviewInfo.adapter = topMarketsAdapter
        rviewInfo2.adapter = globalMarketInfoAdapter
        rviewInfo3.adapter = coinMarketDetailsAdapter
        rviewInfo4.adapter = coinSearchAdapter
        rviewInfoDefi.adapter = topDefiMarketsAdapter

        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.news.observe(viewLifecycleOwner, Observer {
            if(it.size > 0) {
                val newsItem = it.first()
                val sbar = Snackbar.make(this.requireView(), "Title${newsItem.title} - Source:${newsItem.source}", Snackbar.LENGTH_LONG)
                sbar.show()
            }
        })

        viewModel.topMarkets.observe(viewLifecycleOwner, Observer {
            topMarketsAdapter.items = it
            topMarketsAdapter.notifyDataSetChanged()
        })

        viewModel.topHolders.observe(viewLifecycleOwner, Observer {
            if(it.size > 0) {
                val newsItem = it.first()
                val sbar = Snackbar.make(this.requireView(), "Title${newsItem.address} - Source:${newsItem.share}", Snackbar.LENGTH_LONG)
                sbar.show()
            }
        })

        viewModel.coinMarketPoints.observe(viewLifecycleOwner, Observer {
            if(it.size > 0) {
                val newsItem = it.first()
                val sbar = Snackbar.make(this.requireView(), "Date${newsItem.timestamp} - Volume/MCap:${newsItem.volume24h}", Snackbar.LENGTH_LONG)
                sbar.show()
            }
        })

        viewModel.auditorsData.observe(viewLifecycleOwner, Observer {
            if(it.size > 0) {
                val item = it.first()
                val sbar = Snackbar.make(this.requireView(), "Auditor${item.name} - Repors :${item.reports.first().name}", Snackbar.LENGTH_LONG)
                sbar.show()
            }
        })

        viewModel.topDefiMarkets.observe(viewLifecycleOwner, Observer {
            topDefiMarketsAdapter.items = it
            topDefiMarketsAdapter.notifyDataSetChanged()
        })

        viewModel.globalMarketInfo.observe(viewLifecycleOwner, Observer {
            globalMarketInfoAdapter.items = it
            globalMarketInfoAdapter.notifyDataSetChanged()
        })

        viewModel.coinMarketDetails.observe(viewLifecycleOwner, Observer {
            coinMarketDetailsAdapter.items = it
            coinMarketDetailsAdapter.notifyDataSetChanged()
        })

        viewModel.tvlData.observe(viewLifecycleOwner, Observer {
            val snackbar = Snackbar.make(this.requireView(), "${it.data.title} - Tvl:${it.tvl}", Snackbar.LENGTH_LONG)
            snackbar.show()
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