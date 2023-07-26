/*
 * Copyright (C) 2023 ErfanSn
 *
 * AR Touch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AR Touch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AR Touch.  If not, see <https://www.gnu.org/licenses/>.
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
