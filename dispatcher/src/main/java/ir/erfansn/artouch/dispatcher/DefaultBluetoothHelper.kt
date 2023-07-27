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

package ir.erfansn.artouch.dispatcher

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

internal class DefaultBluetoothHelper(private val context: Context) : BluetoothHelper {

    private val bluetoothManager = context.getSystemService<BluetoothManager>()!!
    private val bluetoothAdapter = bluetoothManager.adapter

    override val enabled get() = bluetoothAdapter.isEnabled

    override val allPermissionsGranted
        get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            true
        } else {
            BLUETOOTH_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

    @SuppressLint("MissingPermission")
    override val bondedDevices = flow {
        while (true) {
            runCatching {
                bluetoothAdapter.bondedDevices
            }.getOrDefault(emptySet()).map {
                BondedDevice(device = it)
            }.run {
                emit(this)
            }
            delay(1000)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private val BLUETOOTH_PERMISSIONS = arrayOf(
    Manifest.permission.BLUETOOTH_ADVERTISE,
    Manifest.permission.BLUETOOTH_CONNECT,
)
