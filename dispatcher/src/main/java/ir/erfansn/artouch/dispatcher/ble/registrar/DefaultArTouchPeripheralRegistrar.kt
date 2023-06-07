package ir.erfansn.artouch.dispatcher.ble.registrar

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
import ir.erfansn.artouch.dispatcher.ble.ArTouchSpecification

@SuppressLint("MissingPermission")
internal class DefaultArTouchPeripheralRegistrar(private val context: Context) : ArTouchPeripheralRegistrar {

    var onRegistered: (BluetoothHidDevice) -> Unit = { }

    private val bluetoothManager = context.getSystemService<BluetoothManager>()!!
    private val bluetoothAdapter = bluetoothManager.adapter

    private lateinit var hidProxy: BluetoothHidDevice

    override fun registerDevice() {
        val serviceListener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                registerArTouch(proxy as BluetoothHidDevice)
            }

            override fun onServiceDisconnected(profile: Int) {
                Log.i(TAG, "The Bluetooth HID profile is disconnected")
            }
        }
        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    }

    private fun registerArTouch(hidDevice: BluetoothHidDevice) {
        hidDevice.registerApp(
            BluetoothHidDeviceAppSdpSettings(
                ArTouchSpecification.NAME,
                ArTouchSpecification.DESCRIPTION,
                ArTouchSpecification.PROVIDER,
                ArTouchSpecification.SUBCLASS,
                ArTouchSpecification.REPORT_DESCRIPTOR
            ),
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
                override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                    if (!registered) return

                    onRegistered(hidDevice)
                    hidProxy = hidDevice
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
        private const val TAG = "DefaultArTouchPeripheralRegistrar"
    }
}
