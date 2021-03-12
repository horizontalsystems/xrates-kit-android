package io.horizontalsystems.xrateskit.demo.chartdemo

import androidx.lifecycle.MutableLiveData
import io.horizontalsystems.xrateskit.entities.ChartType

class ChartActivityView {
    val showSpinner = MutableLiveData<Unit>()
    val hideSpinner = MutableLiveData<Unit>()
    val setSelectedPoint = MutableLiveData<ChartPointViewItem>()
    val showChartInfo = MutableLiveData<ChartInfoViewItem>()
    val showHistoInfo = MutableLiveData<HistoInfoViewItem>()
    val setDefaultMode = MutableLiveData<ChartType>()

    val showEma = MutableLiveData<Boolean>()
    val showMacd = MutableLiveData<Boolean>()
    val showRsi = MutableLiveData<Boolean>()

    fun showSpinner() {
        showSpinner.postValue(Unit)
    }

    fun showHistoInfo(info: HistoInfoViewItem) {
        showHistoInfo.postValue(info)
    }

    fun hideSpinner() {
        hideSpinner.postValue(Unit)
    }

    fun setChartType(type: ChartType) {
        setDefaultMode.postValue(type)
    }

    fun showChartInfo(viewItem: ChartInfoViewItem) {
        showChartInfo.postValue(viewItem)
    }

    fun showSelectedPointInfo(item: ChartPointViewItem) {
        setSelectedPoint.postValue(item)
    }

    fun setEmaEnabled(enabled: Boolean) {
        showEma.postValue(enabled)
    }

    fun setMacdEnabled(enabled: Boolean) {
        showMacd.postValue(enabled)
    }

    fun setRsiEnabled(enabled: Boolean) {
        showRsi.postValue(enabled)
    }
}
