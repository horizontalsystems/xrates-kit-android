package io.horizontalsystems.xrateskit.demo.chartdemo

import androidx.lifecycle.MutableLiveData
import io.horizontalsystems.chartview.ChartView
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.CurrencyValue
import io.horizontalsystems.xrateskit.entities.ChartInfo

class ChartActivityView {
    val chartInfoLiveData = MutableLiveData<ChartInfo>()
    val setSelectedPoint = MutableLiveData<Triple<Long, CurrencyValue, ChartView.ChartType>>()

    fun updateChart(chartInfo: ChartInfo) {
        chartInfoLiveData.postValue(chartInfo)
    }

    fun showSelectedPoint(data: Triple<Long, CurrencyValue, ChartView.ChartType>) {
        setSelectedPoint.postValue(data)
    }
}
