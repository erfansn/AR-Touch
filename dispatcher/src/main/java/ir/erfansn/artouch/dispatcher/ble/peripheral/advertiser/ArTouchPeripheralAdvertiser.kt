package ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.util.Log
import androidx.core.content.getSystemService
import ir.erfansn.artouch.dispatcher.ble.ArTouchSpecification
import ir.erfansn.artouch.dispatcher.ble.peripheral.DefaultBleHidPeripheralManager
import ir.erfansn.artouch.dispatcher.ble.peripheral.BleHidPeripheralRegistrar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
internal class ArTouchPeripheralAdvertiser(context: Context) : BleHidPeripheralAdvertiser {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val bleHidPeripheralManager: BleHidPeripheralRegistrar = DefaultBleHidPeripheralManager(context)

    private val bluetoothManager = context.getSystemService<BluetoothManager>()!!
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private val bleAdvertiser: BluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

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
        coroutineScope.launch {
            bluetoothAdapter.name = ArTouchSpecification.NAME
            bleHidPeripheralManager.registerDevice(
                sdpSettings = BluetoothHidDeviceAppSdpSettings(
                    ArTouchSpecification.NAME,
                    ArTouchSpecification.DESCRIPTION,
                    ArTouchSpecification.PROVIDER,
                    ArTouchSpecification.SUBCLASS,
                    ArTouchSpecification.REPORT_DESCRIPTOR
                )
            )
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
        bleHidPeripheralManager.unregisterDevice()
    }

    override fun close() {
        coroutineScope.cancel()
    }

    companion object {
        private const val TAG = "ArTouchPeripheralAdvertiser"
    }
}
