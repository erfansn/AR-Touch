package ir.erfansn.artouch.benchmark.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ir.erfansn.artouch.producer.detector.util.ImageRotationHelper
import org.koin.dsl.bind
import org.koin.dsl.module

val testModule = module {
    single { ApplicationProvider.getApplicationContext<Context>() } bind Context::class
    factory { StubImageRotationHelper() } bind ImageRotationHelper::class
}
