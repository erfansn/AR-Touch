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

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters

// If you have a Xiaomi device and cannot run the tests, follow the steps below:
//  1. Click gutter icon to run whole tests
//  2. When running a_fakeTest enter follow command in terminal
//  "adb shell am start -n 'ir.erfansn.artouch/androidx.test.core.app.InstrumentationActivityInvoker\$BootstrapActivity'"
//  3. Wait to complete tests
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RequestPermissionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val instrumentation by lazy { InstrumentationRegistry.getInstrumentation() }

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ComposeableUnderTest()
        }
    }

    @Test
    fun a_fakeTest() = Unit

    @Test
    fun doesNotHappeningAnything_whenIgnoresFirstRequestPermission() {
        composeTestRule.onNodeWithText("Request").performClick()
        UiDevice.getInstance(instrumentation).pressBack()
        composeTestRule.onNodeWithTag("permission_status").assertDoesNotExist()
    }

    @Test
    fun showsGrantedMessage_whenGrantingItInFirstRequest() {
        composeTestRule.onNodeWithText("Request").performClick()
        instrumentation.grantPermissionInDialog()
        composeTestRule.onNodeWithTag("permission_status").assert(hasText("Granted"))
    }

    @Test
    fun showsGrantedMessage_whenRequestAgainAfterShowingRationaleDescription() {
        composeTestRule.onNodeWithText("Request").performClick()
        instrumentation.denyPermissionInDialog()
        composeTestRule.onNodeWithTag("permission_status").assert(hasText("Show Rationale"))

        composeTestRule.onNodeWithText("Request").performClick()
        instrumentation.grantPermissionInDialog()
        composeTestRule.onNodeWithTag("permission_status").assert(hasText("Granted"))
    }

    @Test
    fun showsGrantedMessage_whenAllowingItFromAppSettingsAfterDenyingFirstRequest() {
        composeTestRule.onNodeWithText("Request").performClick()
        instrumentation.denyPermissionInDialog()
        composeTestRule.onNodeWithTag("permission_status").assert(hasText("Show Rationale"))

        instrumentation.grantPermissionProgrammatically()
        // Simulate app coming from the background
        composeTestRule.activityRule.scenario.apply {
            moveToState(Lifecycle.State.STARTED)
            moveToState(Lifecycle.State.RESUMED)
        }
        composeTestRule.activityRule.scenario.onActivity {
            it.setContent {
                ComposeableUnderTest()
            }
        }

        composeTestRule.onNodeWithText("Request").performClick()
        composeTestRule.onNodeWithTag("permission_status").assert(hasText("Granted"))
    }

    @Test
    fun showsPermanentlyDeniedMessage_whenNotifyingDoNotAskAgainPermission() {
        composeTestRule.onNodeWithText("Request").performClick()
        instrumentation.denyPermissionInDialog()
        composeTestRule.onNodeWithTag("permission_status").assert(hasText("Show Rationale"))

        composeTestRule.onNodeWithText("Request").performClick()
        instrumentation.doNotAskAgainPermissionInDialog()
        composeTestRule.onNodeWithTag("permission_status").assert(hasText("Permanently Denied"))
    }

    @Test
    fun showsRationaleMessage_whenDeniedItInFirstRequest() {
        composeTestRule.onNodeWithText("Request").performClick()
        instrumentation.denyPermissionInDialog()
        composeTestRule.onNodeWithTag("permission_status").assert(hasText("Show Rationale"))
    }

    @Ignore("Should do it manually because of process death after revoking permission")
    @Test
    fun showsRationaleMessage_whenRequestPermissionAfterRevokeItFromAppSettings() {
        composeTestRule.onNodeWithText("Request").performClick()
        instrumentation.grantPermissionInDialog()
        composeTestRule.onNodeWithTag("permission_status").assert(hasText("Granted"))

        instrumentation.revokePermissionProgrammatically()
        // Launch app again
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) }
            .run(context::startActivity)

        composeTestRule.onNodeWithText("Request").performClick()
        composeTestRule.onNodeWithTag("permission_status").assert(hasText("Show Rationale"))
    }
}

@Composable
private fun ComposeableUnderTest() {
    Column {
        var permissionStatus by remember { mutableStateOf<String?>(null) }
        val permissionsRequest = rememberPermissionsRequestLauncher(
            onGranted = {
                permissionStatus = "Granted"
            },
            onRationaleShow = {
                permissionStatus = "Show Rationale"
            },
            onPermanentlyDenied = {
                permissionStatus = "Permanently Denied"
            }
        )
        permissionStatus?.let {
            Text(
                modifier = Modifier.testTag("permission_status"),
                text = it
            )
        }
        Button(onClick = { permissionsRequest.launch(arrayOf(Manifest.permission.CAMERA)) }) {
            Text(text = "Request")
        }
    }
}
