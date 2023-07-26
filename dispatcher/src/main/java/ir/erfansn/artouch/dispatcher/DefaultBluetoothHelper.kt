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
