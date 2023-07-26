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
