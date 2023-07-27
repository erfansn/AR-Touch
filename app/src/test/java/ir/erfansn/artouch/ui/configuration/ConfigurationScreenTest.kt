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

package ir.erfansn.artouch.ui.configuration

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import ir.erfansn.artouch.R
import ir.erfansn.artouch.dispatcher.BondedDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfigurationScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }

    @Test
    fun showsTurnOnBluetoothButton_whenUiStateIsBluetoothDisable() {
        composeTestRule.setContent {
            ConfigurationScreen(uiState = ConfigurationUiState.BluetoothDisable)
        }

        composeTestRule.onNodeWithText(activity.getString(R.string.turn_on_bluetooth))
            .assertIsDisplayed()
    }

    @Test
    fun showsRequestBluetoothPermissionsButton_whenUiStateIsBluetoothEnable() {
        composeTestRule.setContent {
            ConfigurationScreen(uiState = ConfigurationUiState.BluetoothEnable)
        }

        composeTestRule.onNodeWithText(activity.getString(R.string.request_bluetooth_permissions))
            .assertIsDisplayed()
    }

    @Test
    fun notifiesUserAboutNoBondedDevice_whenBondedDevicesListIsEmptyInAdvertisingModeUiState() {
        composeTestRule.setContent {
            ConfigurationScreen(
                uiState = ConfigurationUiState.AdvertisingMode,
                bluetoothBondedDevices = emptyList(),
            )
        }

        composeTestRule.onNodeWithText(activity.getString(R.string.bonded_devices_list_is_empty))
            .assertIsDisplayed()
    }

    @Test
    fun showsBoundedDevicesCorrectly_whenBoundedDevicesListIsPassedInAdvertisingModeUiState() {
        composeTestRule.setContent {
            ConfigurationScreen(
                uiState = ConfigurationUiState.AdvertisingMode,
                bluetoothBondedDevices = listOf(
                    BondedDevice(
                        name = "Lenovo",
                        address = "FF:FF:FF:FF:FF:FF"
                    )
                )
            )
        }

        composeTestRule.onNodeWithText(activity.getString(R.string.bonded_devices_list_is_empty))
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag("bonded_devices_list")
            .onChildren()
            .assertCountEquals(1)
    }
}
