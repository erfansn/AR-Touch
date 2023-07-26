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

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
internal class DefaultBleHidPeripheralManager(private val context: Context) : BleHidPeripheralManager {

    private val bluetoothManager = context.getSystemService<BluetoothManager>()!!
    private val bluetoothAdapter = bluetoothManager.adapter

    private lateinit var hidProxy: BluetoothHidDevice

    private val _connectionState = MutableStateFlow(BleHidConnectionState.Disconnected)
    override val connectionState = _connectionState.asStateFlow()

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun trySendReport(
        target: BluetoothDevice,
        id: UByte,
        data: UByteArray,
    ): Boolean {
        if (connectionState.value != BleHidConnectionState.Connected) return false

        return hidProxy.sendReport(
            target,
            id.toInt(),
            data.toByteArray()
        )
    }

    override fun connect(centralDevice: BluetoothDevice) {
        check(::hidProxy.isInitialized) { "Must first register the device" }

        repeat(MAX_CONNECTION_TRY) {
            if (hidProxy.connect(centralDevice)) return
        }
        _connectionState.update { BleHidConnectionState.FailedToConnect }
    }

    override fun disconnect(centralDevice: BluetoothDevice) {
        if (!::hidProxy.isInitialized) return

        hidProxy.disconnect(centralDevice)
    }

    override suspend fun registerDevice(sdpSettings: BluetoothHidDeviceAppSdpSettings) {
        hidProxy = prepareHidProxy()
        hidProxy.registerApp(sdpSettings)
    }

    private suspend fun prepareHidProxy() = suspendCancellableCoroutine {
        val serviceListener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                Log.i(TAG, "onServiceConnected Called")
                if (it.isActive) it.resume(proxy as BluetoothHidDevice)
            }

            override fun onServiceDisconnected(profile: Int) {
                Log.i(TAG, "The Bluetooth HID profile is disconnected")
                if (it.isActive) it.cancel()
            }
        }
        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    }

    private suspend fun BluetoothHidDevice.registerApp(sdpSettings: BluetoothHidDeviceAppSdpSettings) = suspendCancellableCoroutine {
        registerApp(
            sdpSettings,
            null,
            BluetoothHidDeviceAppQosSettings(
                BluetoothHidDeviceAppQosSettings.SERVICE_GUARANTEED,
                8000,
                800,
                16000,
                20,
                5,
            ),
            ContextCompat.getMainExecutor(context),
            object : BluetoothHidDevice.Callback() {
                override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                    if (state == BluetoothProfile.STATE_DISCONNECTING) return

                    _connectionState.update {
                        when (state) {
                            BluetoothProfile.STATE_DISCONNECTED -> BleHidConnectionState.Disconnected
                            BluetoothProfile.STATE_CONNECTING -> BleHidConnectionState.Connecting
                            BluetoothProfile.STATE_CONNECTED -> BleHidConnectionState.Connected
                            else -> throw IllegalStateException()
                        }
                    }
                    Log.d(TAG, "Bluetooth current state is ${_connectionState.value}")
                }

                override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                    if (it.isActive && registered) it.resume(Unit)
                }
            }
        )
    }

    override fun unregisterDevice() {
        if (!::hidProxy.isInitialized) return

        hidProxy.unregisterApp()
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidProxy)
    }

    companion object {
        private const val TAG = "DefaultBleHidPeripheralManager"

        private const val MAX_CONNECTION_TRY = 5
    }
}
