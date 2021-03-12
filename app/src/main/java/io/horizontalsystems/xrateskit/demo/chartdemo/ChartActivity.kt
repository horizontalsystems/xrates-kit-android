package io.horizontalsystems.xrateskit.demo.chartdemo

import android.os.Bundle
import android.text.format.DateFormat
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.chartview.Chart
import io.horizontalsystems.chartview.models.PointInfo
import io.horizontalsystems.xrateskit.demo.App
import io.horizontalsystems.xrateskit.demo.R
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.CurrencyValue
import kotlinx.android.synthetic.main.activity_chart.*
import java.text.SimpleDateFormat
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.Currency
import io.horizontalsystems.xrateskit.entities.ChartType
import java.util.*

class ChartActivity : AppCompatActivity(), Chart.Listener {

    private lateinit var presenter: ChartPresenter
    private val formatter = App.numberFormatter
    private var actions = mapOf<ChartType, View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        presenter = ViewModelProvider(this, ChartPresenter.Factory()).get(ChartPresenter::class.java)
        chart.setListener(this)
        chart.rateFormatter = presenter.rateFormatter
        observeData()
        bindActions()
        presenter.onLoad()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onTouchDown() {
        setViewVisibility(coinChartView, isVisible = true)
    }

    override fun onTouchSelect(point: PointInfo) {
        presenter.onTouchSelect(point)
    }

    override fun onTouchUp() {
        setViewVisibility(coinChartView, isVisible = false)
    }

    private fun setViewVisibility(vararg views: View, isVisible: Boolean) {
        views.forEach {
            if (isVisible)
                it.visibility = View.VISIBLE else
                it.visibility = View.INVISIBLE
        }
    }

    //  Date helpers

    private val timeFormat: String by lazy {
        val is24HourFormat = DateFormat.is24HourFormat(this)
        if (is24HourFormat) "HH:mm" else "h:mm a"
    }

    private fun getFullDate(date: Date): String = formatDate(date, "MMM d, yyyy, $timeFormat")
    private fun formatDate(date: Date, pattern: String): String {
        return SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern), Locale.getDefault()).format(date)
    }

    private fun observeData() {

        presenter.view.showHistoInfo.observe(this, {
            txtHistoRate.text = "${it.coinType.ID}/${it.currencyCode} Rate:${it.rate} Date:${Date(it.date * 1000)}"
        })

        presenter.view.showSpinner.observe(this, {
            chart.showSinner()
        })

        presenter.view.hideSpinner.observe(this, {
            chart.hideSinner()
        })

        presenter.view.setDefaultMode.observe(this, { type ->
            actions[type]?.let { resetActions(it, setDefault = true) }
        })

        presenter.view.showChartInfo.observe(this, { item ->
            chart.showChart(true)

            rootView.post {
                chart.setData(item.chartData, item.chartType)
            }

            emaChartIndicator.bind(item.emaTrend)
            macdChartIndicator.bind(item.macdTrend)
            rsiChartIndicator.bind(item.rsiTrend)
        })


        presenter.view.setSelectedPoint.observe(this, { item ->
            pointInfoVolume.isInvisible = true
            pointInfoVolumeTitle.isInvisible = true

            macdHistogram.isInvisible = true
            macdSignal.isInvisible = true
            macdValue.isInvisible = true

            pointInfoDate.text = getFullDate(Date(item.date * 1000))
            pointInfoPrice.text = formatter.formatForRates(CurrencyValue(Currency(item.price.currency.symbol, ""), item.price.value), false, 4)

            item.volume?.let {
                pointInfoVolumeTitle.isVisible = true
                pointInfoVolume.isVisible = true
                pointInfoVolume.text = formatter.formatForRates(CurrencyValue(Currency(item.price.currency.symbol, ""), item.volume.value), false, 4)
            }

            item.macdInfo?.let { macdInfo ->
                macdInfo.histogram?.let {
                    macdHistogram.isVisible = true
                    macdHistogram.text = formatter.formatForRates(CurrencyValue(Currency(item.price.currency.symbol, ""), it.toBigDecimal()), false, 2)
                }
                macdInfo.signal?.let {
                    macdSignal.isVisible = true
                    macdSignal.text = formatter.formatForRates(CurrencyValue(Currency(item.price.currency.symbol, ""), it.toBigDecimal()), false, 2)
                }
                macdInfo.macd?.let {
                    macdValue.isVisible = true
                    macdValue.text = formatter.formatForRates(CurrencyValue(Currency(item.price.currency.symbol, ""), it.toBigDecimal()), false, 2)
                }
            }
        })

        presenter.view.showEma.observe(this, { enabled ->
            chart.showEma(enabled)
            emaChartIndicator.setStateEnabled(enabled)
        })

        presenter.view.showMacd.observe(this, { enabled ->
            chart.showMacd(enabled)
            macdChartIndicator.setStateEnabled(enabled)

            setViewVisibility(pointInfoVolume, pointInfoVolumeTitle, isVisible = !enabled)
            setViewVisibility(macdSignal, macdHistogram, macdValue, isVisible = enabled)
        })

        presenter.view.showRsi.observe(this, { enabled ->
            chart.showRsi(enabled)
            rsiChartIndicator.setStateEnabled(enabled)
        })
    }

    private fun bindActions() {
        actions = mapOf(
            Pair(ChartType.TODAY, buttonToday),
            Pair(ChartType.DAILY, button24),
            Pair(ChartType.WEEKLY, button1W),
            Pair(ChartType.WEEKLY2, button2W),
            Pair(ChartType.MONTHLY, button1M),
            Pair(ChartType.MONTHLY3, button3M),
            Pair(ChartType.MONTHLY6, button6M),
            Pair(ChartType.MONTHLY12, button1Y),
            Pair(ChartType.MONTHLY24, button2Y)
        )

        actions.forEach { (type, action) ->
            action.setOnClickListener { view ->
                presenter.onSelect(type)
                resetActions(view)
            }
        }

        emaChartIndicator.setOnClickListener {
            presenter.toggleEma()
        }

        macdChartIndicator.setOnClickListener {
            presenter.toggleMacd()
        }

        rsiChartIndicator.setOnClickListener {
            presenter.toggleRsi()
        }

    }

    private fun resetActions(current: View, setDefault: Boolean = false) {
        actions.values.forEach { it.isActivated = false }
        current.isActivated = true

        val inLeftSide = chartActions.width / 2 < current.left
        if (setDefault) {
            chartActionsWrap.scrollTo(if (inLeftSide) chartActions.width else 0, 0)
            return
        }

        val by = if (inLeftSide) {
            chartActions.scrollX + current.width
        } else {
            chartActions.scrollX - current.width
        }

        chartActionsWrap.smoothScrollBy(by, 0)
    }

}
