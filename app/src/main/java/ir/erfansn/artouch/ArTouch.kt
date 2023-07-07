package ir.erfansn.artouch

import android.app.Application
import ir.erfansn.artouch.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ArTouch : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ArTouch)

            modules(appModule)
        }
    }
}
