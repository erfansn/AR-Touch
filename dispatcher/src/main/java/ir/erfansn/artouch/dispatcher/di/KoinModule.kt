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
