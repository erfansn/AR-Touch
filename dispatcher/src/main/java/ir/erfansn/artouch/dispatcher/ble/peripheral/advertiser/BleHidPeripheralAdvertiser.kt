package ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

interface BleHidPeripheralAdvertiser : DefaultLifecycleObserver {
    fun startAdvertising()
    fun stopAdvertising()

    override fun onStart(owner: LifecycleOwner) = startAdvertising()
    override fun onStop(owner: LifecycleOwner) = stopAdvertising()
}
