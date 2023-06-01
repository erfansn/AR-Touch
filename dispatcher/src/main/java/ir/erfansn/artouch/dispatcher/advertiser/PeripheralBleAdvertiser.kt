package ir.erfansn.artouch.dispatcher.advertiser

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

interface PeripheralBleAdvertiser : DefaultLifecycleObserver {
    fun startAdvertising()
    fun stopAdvertising()

    override fun onStart(owner: LifecycleOwner) = startAdvertising()
    override fun onStop(owner: LifecycleOwner) = stopAdvertising()
}
