package ir.erfansn.artouch.dispatcher

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
data class BondedDevice(
    val device: BluetoothDevice? = null,
    val name: String = device?.name.orEmpty(),
    val address: String = device?.address.orEmpty(),
)
