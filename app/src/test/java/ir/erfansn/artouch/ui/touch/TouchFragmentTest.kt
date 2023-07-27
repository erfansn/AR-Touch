/*
 * Copyright 2023 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.erfansn.artouch.ui.touch

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.GrantPermissionRule
import ir.erfansn.artouch.R
import ir.erfansn.artouch.di.FakeArTouchPeripheralDevice
import ir.erfansn.artouch.di.appModule
import ir.erfansn.artouch.di.testModule
import ir.erfansn.artouch.dispatcher.ble.peripheral.BleHidConnectionState
import ir.erfansn.artouch.ui.configuration.ConfigurationFragment
import kotlinx.coroutines.flow.update
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTestRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class TouchFragmentTest {

    private val fakeArTouchPeripheralDevice = FakeArTouchPeripheralDevice()

    @get:Rule(order = 0)
    val koinTestRule = KoinTestRule.create {
        modules(appModule, testModule(fakeArTouchPeripheralDevice))
    }

    @get:Rule(order = 1)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        ConfigurationFragment.CAMERA_PERMISSION,
        *ConfigurationFragment.BLUETOOTH_PERMISSIONS,
    )

    @Before
    fun launchFragment() {
        launchFragmentInContainer<TouchFragment>(
            fragmentArgs = bundleOf(TouchFragment.DEBUG_MODE_KEY to false),
            themeResId = R.style.Theme_ARTouch,
        )
    }

    @Test
    fun notifiesAboutDisconnected_whenBleHidConnectionStateIsDisconnected() {
        fakeArTouchPeripheralDevice.connectionState.update { BleHidConnectionState.Disconnected }

        onView(withId(R.id.connecting_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.user_message)).check(matches(withText(R.string.device_is_disconnected)))
        onView(withId(R.id.reconnect)).check(matches(isDisplayed()))
        onView(withId(R.id.utility_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.select_another_device)))
    }

    @Test
    fun notifiesAboutConnecting_whenBleHidConnectionStateIsConnecting() {
        fakeArTouchPeripheralDevice.connectionState.update { BleHidConnectionState.Connecting }

        onView(withId(R.id.connecting_indicator)).check(matches(isDisplayed()))
        onView(withId(R.id.user_message)).check(matches(withText(R.string.connecting)))
        onView(withId(R.id.reconnect)).check(matches(not(isDisplayed())))
        onView(withId(R.id.utility_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.cancel)))
    }

    @Test
    fun notifiesAboutErrorInConnecting_whenBleHidConnectionStateIsFailedToConnect() {
        fakeArTouchPeripheralDevice.connectionState.update { BleHidConnectionState.FailedToConnect }

        onView(withId(R.id.connecting_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.user_message)).check(matches(withText(R.string.error_when_connecting)))
        onView(withId(R.id.reconnect)).check(matches(not(isDisplayed())))
        onView(withId(R.id.utility_button))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.select_another_device)))
    }

    @Config(shadows = [ShadowProcessCameraProvider::class])
    @Test
    fun showsCameraPreview_whenBleHidConnectionStateIsConnected() {
        fakeArTouchPeripheralDevice.connectionState.update { BleHidConnectionState.Connected }

        onView(withId(R.id.connection_state)).check(matches(not(isDisplayed())))
    }
}
