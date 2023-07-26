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

import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.dispatcher.BondedDevice
import ir.erfansn.artouch.dispatcher.ble.peripheral.BleHidConnectionState
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.ArTouchPeripheralDevice
import kotlinx.coroutines.flow.MutableStateFlow

class FakeArTouchPeripheralDevice : ArTouchPeripheralDevice {

    override lateinit var centralDevice: BondedDevice

    override val connectionState = MutableStateFlow(BleHidConnectionState.Disconnected)

    override fun connect() = Unit

    override fun disconnect() = Unit

    override fun dispatchTouch(tapped: Boolean, point: Point) = Unit

    override fun close() = Unit
}
