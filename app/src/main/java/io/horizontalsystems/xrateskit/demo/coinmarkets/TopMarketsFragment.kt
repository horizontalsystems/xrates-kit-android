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
import io.horizontalsystems.xrateskit.demo.R
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.CoinType
import io.horizontalsystems.xrateskit.entities.TimePeriod
import kotlinx.android.synthetic.main.fragment_top_markets.*


class TopMarketsFragment() : Fragment() {

    private val viewModel by viewModels<TopMarketsViewModel> { TopMarketsModule.Factory() }
    private val globalMarketInfoAdapter = GlobalCoinsMarketAdapter()
    private val topMarketsAdapter = TopMarketsAdapter()

    private val coins = listOf(Coin("BTC", "Bitcoin", CoinType.Bitcoin),
                               Coin("ETH", "Ethereum", CoinType.Ethereum),
                               Coin("BCH", "Bitcoin-Cash", CoinType.BitcoinCash),
                               Coin("DASH","Dash", CoinType.Dash),
                               Coin("BNB", "BinanceChain",  CoinType.Binance),
                               Coin("EOS", "Eos",  CoinType.Eos),
                               Coin("ZRX", "Zrx", CoinType.Erc20("0xE41d2489571d322189246DaFA5ebDe1F4699F498")),
                               Coin("ELF", "Elf", CoinType.Erc20("0xbf2179859fc6D5BEE9Bf9158632Dc51678a4100e")),
                               Coin("GNT", "Gnt", CoinType.Erc20("0xa74476443119A942dE498590Fe1f2454d7D4aC0d")),
                               Coin("HOT", "Hot", CoinType.Erc20("0x6c6EE5e31d828De241282B9606C8e98Ea48526E2")),
                               Coin("ADAI", "Aave DAI", CoinType.Erc20("0xfC1E690f61EFd961294b3e1Ce3313fBD8aa4f85d")),
                               Coin("BNT", "Bnt", CoinType.Erc20("0x1F573D6Fb3F13d689FF844B4cE37794d79a7FF1C")))


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
            viewModel.loadFavorites(coins.map { it.code }, spAct.timePeriod)
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
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
            rviewInfo2.visibility = View.GONE
            rviewInfo.visibility = View.VISIBLE
        }

        rviewInfo.adapter = topMarketsAdapter
        rviewInfo2.adapter = globalMarketInfoAdapter

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