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
