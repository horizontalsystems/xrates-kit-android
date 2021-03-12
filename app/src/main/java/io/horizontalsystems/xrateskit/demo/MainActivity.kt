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
                               CoinData(CoinType.fromString("unsupported|theta-token"),"THETA", "THETA"),
                               CoinData(CoinType.Erc20("0x5732046a883704404f284ce41ffadd5b007fd668"),"BLZ", "BLZ"),
                               CoinData(CoinType.Erc20("0x1f9840a85d5af5bf1d1762f925bdaddc4201f984"), "UNI", "Uniswap"),
                               CoinData(CoinType.Bep20("0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82"),"CAKE", "Cake"))


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
