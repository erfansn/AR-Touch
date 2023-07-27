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

package ir.erfansn.artouch.dispatcher.di

import ir.erfansn.artouch.dispatcher.BluetoothHelper
import ir.erfansn.artouch.dispatcher.DefaultBluetoothHelper
import ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser.ArTouchPeripheralAdvertiser
import ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser.BleHidPeripheralAdvertiser
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.ArTouchPeripheralDevice
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.DefaultArTouchPeripheralDevice
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dispatcherModule = module {
    factoryOf(::DefaultBluetoothHelper) bind BluetoothHelper::class
    factoryOf(::ArTouchPeripheralAdvertiser) bind BleHidPeripheralAdvertiser::class
    factoryOf(::DefaultArTouchPeripheralDevice) bind ArTouchPeripheralDevice::class
}
