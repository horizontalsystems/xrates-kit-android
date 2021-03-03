package io.horizontalsystems.xrateskit.demo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.demo.chartdemo.ChartActivity
import io.horizontalsystems.xrateskit.demo.coinmarkets.TopMarketsFragment
import io.horizontalsystems.xrateskit.entities.CoinData
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var presenter: CoinsPresenter

    private val coinDatas = listOf(CoinData(CoinType.Bitcoin, "BTC", "Bitcoin"),
                               CoinData(CoinType.Ethereum, "ETH", "Ethereum",),
                               CoinData(CoinType.BitcoinCash, "BCH", "Bch"),
                               CoinData(CoinType.Dash, "DASH","Dash"),
                               CoinData(CoinType.fromString("bep2|BNB"), "BNB", "Bnb"),
                               CoinData(CoinType.fromString("unsupprted|eos"),"EOS", "Eos"),
                               CoinData(CoinType.Erc20("0xE41d2489571d322189246DaFA5ebDe1F4699F498"),"ZRX", "Zrx"),
                               CoinData(CoinType.Erc20("0xbf2179859fc6D5BEE9Bf9158632Dc51678a4100e"),"ELF", "Elf"),
                               CoinData(CoinType.Erc20("0xa74476443119A942dE498590Fe1f2454d7D4aC0d"), "GNT", "Gnt"),
                               CoinData(CoinType.Erc20("0x6c6EE5e31d828De241282B9606C8e98Ea48526E2"),"HOT", "Hot"),
                               CoinData(CoinType.Erc20("0xfC1E690f61EFd961294b3e1Ce3313fBD8aa4f85d"),"ADAI", "Aave DAI"),
                               CoinData(CoinType.Erc20("0x1F573D6Fb3F13d689FF844B4cE37794d79a7FF1C"),"BNT", "Bnt"))


    private val coinsAdapter = RatesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = ViewModelProvider(this, CoinsPresenter.Factory()).get(CoinsPresenter::class.java)
        presenter.onLoad(coinDatas)

        coinsAdapter.presenter = presenter
        coinsRecyclerView.adapter = coinsAdapter

        observeLiveData()
        configureView()

        RxJavaPlugins.setErrorHandler {}
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_chart -> {
                val intent = Intent(this, ChartActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_topMarkets -> {

                val fragment: Fragment = TopMarketsFragment()
                this.supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        fragment
                    )
                    .addToBackStack("TopMarkets")
                    .commit()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeLiveData() {
        presenter.view.reload.observe(this, Observer {
            coinsAdapter.notifyDataSetChanged()
        })

        presenter.view.updateCoins.observe(this, Observer {
            coinsAdapter.items = it
            coinsAdapter.notifyDataSetChanged()
        })
    }

    private fun configureView() {
        currency.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, presenter.currencies)
        currency.isSelected = false
        currency.setSelection(0, false)
        currency.onItemSelectedListener = this

        refresh.setOnClickListener {
            presenter.onRefresh()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        presenter.onChangeCurrency(currency.selectedItem as String)
    }
}
