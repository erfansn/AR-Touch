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
import ir.erfansn.artouch.dispatcher.ble.ArTouchSpecification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
internal class ArTouchPeripheralManager(
    private val context: Context,
    private val scope: CoroutineScope,
) : BleHidPeripheralManager {

    private val bluetoothManager = context.getSystemService<BluetoothManager>()!!
    private val bluetoothAdapter = bluetoothManager.adapter

    private lateinit var hidProxy: BluetoothHidDevice

    private val _connectionState = MutableStateFlow(BleHidConnectionState.Disconnected)
    override val connectionState = _connectionState.asStateFlow()

    private var isRegistered = false

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

    override suspend fun registerDevice() = suspendCancellableCoroutine {
        val serviceListener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                Log.i(TAG, "onServiceConnected Called")
                scope.launch {
                    hidProxy = proxy as BluetoothHidDevice
                    withTimeoutOrNull(2500) {
                        hidProxy.registerArTouch()
                    }?.let { _ ->
                        if (!isRegistered) return@let

                        it.resume(Unit)
                    } ?: run {
                        it.cancel()
                    }
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                Log.i(TAG, "The Bluetooth HID profile is disconnected")
                isRegistered = false
            }
        }
        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
        isRegistered = true
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
                    if (!registered) return

                    it.resume(Unit)
                }
            }
        )
    }

    override fun unregisterDevice() {
        if (!::hidProxy.isInitialized) return

        hidProxy.unregisterApp()
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidProxy)
        isRegistered = false
    }

    companion object {
        private const val TAG = "ArTouchPeripheralManager"

        private const val MAX_CONNECTION_TRY = 5
    }
}
