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

package ir.erfansn.artouch.dispatcher.ble.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import kotlinx.coroutines.flow.StateFlow

internal interface BleHidPeripheralRegistrar {
    suspend fun registerDevice(sdpSettings: BluetoothHidDeviceAppSdpSettings)
    fun unregisterDevice()
}

internal interface BleHidPeripheralManager : BleHidPeripheralRegistrar {
    val connectionState: StateFlow<BleHidConnectionState>
    @OptIn(ExperimentalUnsignedTypes::class)
    fun trySendReport(target: BluetoothDevice, id: UByte, data: UByteArray): Boolean
    fun connect(centralDevice: BluetoothDevice)
    fun disconnect(centralDevice: BluetoothDevice)
}
