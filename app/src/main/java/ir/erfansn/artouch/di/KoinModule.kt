/*
 * Copyright (C) 2023 ErfanSn
 *
 * AR Touch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AR Touch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AR Touch.  If not, see <https://www.gnu.org/licenses/>.
 */

package ir.erfansn.artouch.di

import ir.erfansn.artouch.dispatcher.di.dispatcherModule
import ir.erfansn.artouch.producer.di.HAND_DETECTOR_QUALIFIER
import ir.erfansn.artouch.producer.di.ARUCO_DETECTOR_QUALIFIER
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
            get(ARUCO_DETECTOR_QUALIFIER),
        )
    }
}
