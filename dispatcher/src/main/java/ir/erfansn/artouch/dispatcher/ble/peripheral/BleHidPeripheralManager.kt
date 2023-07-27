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
