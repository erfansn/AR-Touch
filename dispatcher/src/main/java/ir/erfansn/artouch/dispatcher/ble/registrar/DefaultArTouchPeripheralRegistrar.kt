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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
internal class DefaultArTouchPeripheralRegistrar(
    private val context: Context,
    private val scope: CoroutineScope,
) : ArTouchPeripheralRegistrar {

    private val bluetoothManager = context.getSystemService<BluetoothManager>()!!
    private val bluetoothAdapter = bluetoothManager.adapter

    private lateinit var hidProxy: BluetoothHidDevice

    override val connectionState = MutableStateFlow(ArTouchConnectionState.Disconnected)

    override suspend fun registerDevice() = suspendCancellableCoroutine {
        bluetoothAdapter.name = ArTouchSpecification.NAME

        val serviceListener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                scope.launch {
                    // Set casting result directly to hidProxy
                    val hidProfile = proxy as BluetoothHidDevice
                    withTimeoutOrNull(2500) {
                        hidProfile.registerArTouch()
                    }?.let { _ ->
                        // Resume with Unit
                        it.resume(hidProfile)
                    } ?: run {
                        it.cancel()
                    }
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                Log.i(TAG, "The Bluetooth HID profile is disconnected")
            }
        }
        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    }

    private suspend fun BluetoothHidDevice.registerArTouch() = suspendCancellableCoroutine {
        registerApp(
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
                override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                    if (state == BluetoothProfile.STATE_DISCONNECTING) return

                    connectionState.value = when (state) {
                        BluetoothProfile.STATE_DISCONNECTED -> ArTouchConnectionState.Disconnected
                        BluetoothProfile.STATE_CONNECTING -> ArTouchConnectionState.Connecting
                        BluetoothProfile.STATE_CONNECTED -> ArTouchConnectionState.Connected
                        else -> throw IllegalStateException()
                    }
                }

                override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                    if (!registered) return

                    // Remove this
                    hidProxy = this@registerArTouch
                    it.resume(Unit)
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
