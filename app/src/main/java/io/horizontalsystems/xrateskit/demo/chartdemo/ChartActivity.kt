package io.horizontalsystems.xrateskit.demo.chartdemo

import android.os.Bundle
import android.text.format.DateFormat
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.xrateskit.demo.App
import io.horizontalsystems.xrateskit.demo.R
import io.horizontalsystems.xrateskit.demo.chartdemo.chartview.ChartView
import io.horizontalsystems.xrateskit.demo.chartdemo.chartview.models.ChartPointFloat
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.Currency
import kotlinx.android.synthetic.main.activity_chart.*
import java.text.SimpleDateFormat
import java.util.*


class ChartActivity : AppCompatActivity(), ChartView.Listener {

    private lateinit var presenter: ChartPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        presenter = ViewModelProvider(this, ChartPresenter.Factory()).get(ChartPresenter::class.java)
        presenter.onLoad()

        val currency = Currency(App.baseCurrency, "$")

        chartView.listener = this
        chartView.setIndicator(chartViewIndicator)

        presenter.view.chartInfoLiveData.observe(this, Observer { chartInfo ->

            val chartPoints = chartInfo.points.map {
                ChartPointFloat(it.value.toFloat(), it.volume?.toFloat(), it.timestamp)
            }
            chartView.setData(chartPoints, ChartView.ChartType.DAILY, chartInfo.startTimestamp, chartInfo.endTimestamp, currency)
        })

        presenter.view.setSelectedPoint.observe(this, Observer { (time, value, type) ->
            val dateText = when (type) {
                ChartView.ChartType.DAILY,
                ChartView.ChartType.WEEKLY -> getFullDate(Date(time * 1000))
                else -> getDateWithYear(Date(time * 1000))
            }
            pointInfoPrice.text = App.numberFormatter.formatForRates(value, maxFraction = 8, trimmable = false)
            pointInfoDate.text = dateText
        })
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
        setViewVisibility(chartViewIndicator, isVisible = true)
    }

    override fun onTouchUp() {
        setViewVisibility(chartViewIndicator, isVisible = false)
    }

    override fun onTouchSelect(point: ChartPointFloat) {
        presenter.onTouchSelect(point)
    }

    private fun setViewVisibility(vararg views: View, isVisible: Boolean) {
        views.forEach {
            if (isVisible)
                it.visibility = View.VISIBLE else
                it.visibility = View.INVISIBLE
        }
    }

    //Date helpers

    private val timeFormat: String by lazy {
        val is24HourFormat = DateFormat.is24HourFormat(this)
        if (is24HourFormat) "HH:mm" else "h:mm a"
    }

    private fun getFullDate(date: Date): String = formatDate(date, "MMM d, yyyy, $timeFormat")
    private fun getDateWithYear(date: Date): String = formatDate(date, "MMM d, yyyy")

    private fun formatDate(date: Date, pattern: String): String {
        return SimpleDateFormat(
            DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern),
            Locale.getDefault()).format(date)
    }
}
