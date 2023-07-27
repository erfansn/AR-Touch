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

import android.app.Instrumentation
import android.os.Build
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import java.util.Locale

private const val TEST_PERMISSION = android.Manifest.permission.CAMERA

fun Instrumentation.grantPermissionProgrammatically() {
    uiAutomation.grantRuntimePermission(targetContext.packageName, TEST_PERMISSION)
}

fun Instrumentation.revokePermissionProgrammatically() {
    uiAutomation.revokeRuntimePermission(targetContext.packageName, TEST_PERMISSION)
}

fun Instrumentation.grantPermissionInDialog() {
    val uiDevice = UiDevice.getInstance(this)
    listOf(
        *"allow".anotherStyles(),
        *"allow only while using the app".anotherStyles(),
        *"while using the app".anotherStyles(),
    ).firstNotNullOf(uiDevice::findPermissionButton).click()
}

fun Instrumentation.denyPermissionInDialog() {
    val uiDevice = UiDevice.getInstance(this)
    listOf(
        *"deny".anotherStyles(),
        *"don't allow".anotherStyles(),
        *"don’t allow".anotherStyles(),
    ).firstNotNullOf(uiDevice::findPermissionButton).click()
}

fun Instrumentation.doNotAskAgainPermissionInDialog() {
    val uiDevice = UiDevice.getInstance(this)
    when {
        Build.VERSION.SDK_INT >= 30 -> {
            denyPermissionInDialog()
        }

        Build.VERSION.SDK_INT == 29 -> {
            uiDevice
                .findPermissionButton("Deny & don’t ask again")
                ?.click()
        }

        else -> {
            uiDevice.findObject(By.text("Don't ask again")).click()
            denyPermissionInDialog()
        }
    }
}

private fun UiDevice.findPermissionButton(text: String): UiObject2? =
    findObject(
        By.text(text)
            .clickable(true)
            .clazz("android.widget.Button")
    )

private fun String.anotherStyles(): Array<String> {
    return arrayOf(
        uppercase(),
        replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
        lowercase()
    )
}
