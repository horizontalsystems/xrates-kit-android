package io.horizontalsystems.xrateskit.demo

import android.app.Application
import com.facebook.stetho.Stetho

class App : Application() {

    companion object {
        lateinit var ratesManager: RatesManager
        lateinit var baseCurrency: String
    }

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)

        baseCurrency = "USD"
        ratesManager = RatesManager(this, baseCurrency)
    }
}
