package ir.erfansn.artouch.dispatcher.advertiser

import android.annotation.SuppressLint
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.util.Log
import ir.erfansn.artouch.dispatcher.ArTouchSpecification

@SuppressLint("MissingPermission")
class ArTouchBleAdvertiser(private val bleAdvertiser: BluetoothLeAdvertiser) : PeripheralBleAdvertiser {

    private val advertiseSettings = AdvertiseSettings.Builder()
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setConnectable(true)
        .setTimeout(0)
        .build()
    private val advertiseData = AdvertiseData.Builder()
        .addServiceUuid(ArTouchSpecification.SERVICE_UUID)
        .setIncludeDeviceName(false)
        .build()
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

    override fun startAdvertising() {
        bleAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
    }

    override fun stopAdvertising() {
        bleAdvertiser.stopAdvertising(advertiseCallback)
    }

    companion object {
        private const val TAG = "ArTouchBleAdvertiser"
    }
}
