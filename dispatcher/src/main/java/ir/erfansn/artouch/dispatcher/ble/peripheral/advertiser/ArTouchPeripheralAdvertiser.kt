package ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.util.Log
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleOwner
import ir.erfansn.artouch.dispatcher.ble.ArTouchSpecification
import ir.erfansn.artouch.dispatcher.ble.peripheral.ArTouchPeripheralManager
import ir.erfansn.artouch.dispatcher.ble.peripheral.BleHidPeripheralRegistrar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class ArTouchPeripheralAdvertiser(context: Context) : BleHidPeripheralAdvertiser {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val arTouchPeripheralManager: BleHidPeripheralRegistrar = ArTouchPeripheralManager(
        context = context,
        scope = scope,
    )

    private val bluetoothManager = context.getSystemService<BluetoothManager>()!!
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.i(TAG, "Advertiser started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e(TAG, "Advertiser occurred failure error code is $errorCode")
        }
    }

    init {
        bluetoothAdapter.name = ArTouchSpecification.NAME
    }

    override fun startAdvertising() {
        scope.launch {
            arTouchPeripheralManager.registerDevice()
            bleAdvertiser.startAdvertising(
                AdvertiseSettings.Builder()
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setConnectable(true)
                    .setTimeout(0)
                    .build(),
                AdvertiseData.Builder()
                    .addServiceUuid(ArTouchSpecification.SERVICE_UUID)
                    .setIncludeDeviceName(true)
                    .build(),
                advertiseCallback,
            )
        }
    }

    override fun stopAdvertising() {
        bleAdvertiser.stopAdvertising(advertiseCallback)
        arTouchPeripheralManager.unregisterDevice()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        scope.cancel()
    }

    companion object {
        private const val TAG = "ArTouchPeripheralAdvertiser"
    }
}
