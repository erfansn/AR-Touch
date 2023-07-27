/*
 * Copyright 2023 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
