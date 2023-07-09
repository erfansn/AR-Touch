package ir.erfansn.artouch.dispatcher.di

import ir.erfansn.artouch.dispatcher.BluetoothHelper
import ir.erfansn.artouch.dispatcher.DefaultBluetoothHelper
import ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser.ArTouchPeripheralAdvertiser
import ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser.BleHidPeripheralAdvertiser
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.ArTouchPeripheralDevice
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.DefaultArTouchPeripheralDevice
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dispatcherModule = module {
    factoryOf(::DefaultBluetoothHelper) bind BluetoothHelper::class
    factoryOf(::ArTouchPeripheralAdvertiser) bind BleHidPeripheralAdvertiser::class
    factoryOf(::DefaultArTouchPeripheralDevice) bind ArTouchPeripheralDevice::class
}
