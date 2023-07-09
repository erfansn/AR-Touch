package ir.erfansn.artouch.di

import ir.erfansn.artouch.dispatcher.di.dispatcherModule
import ir.erfansn.artouch.producer.di.HAND_DETECTOR_QUALIFIER
import ir.erfansn.artouch.producer.di.MARKER_DETECTOR_QUALIFIER
import ir.erfansn.artouch.producer.di.producerModule
import ir.erfansn.artouch.ui.configuration.ConfigurationViewModel
import ir.erfansn.artouch.ui.touch.TouchViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    includes(producerModule, dispatcherModule)

    single { Dispatchers.Default }

    viewModelOf(::ConfigurationViewModel)
    viewModel {
        TouchViewModel(
            get(),
            get(),
            get(),
            get(),
            get(HAND_DETECTOR_QUALIFIER),
            get(MARKER_DETECTOR_QUALIFIER),
        )
    }
}
