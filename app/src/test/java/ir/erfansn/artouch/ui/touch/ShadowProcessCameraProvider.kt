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

package ir.erfansn.artouch.ui.touch

import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraEffect
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
import androidx.camera.core.ViewPort
import androidx.camera.core.impl.CameraConfig
import androidx.camera.core.impl.CameraInternal
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.util.LinkedHashSet

@Implements(ProcessCameraProvider::class)
class ShadowProcessCameraProvider {

    @Implementation
    fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        viewPort: ViewPort?,
        effects: List<CameraEffect>,
        vararg useCases: UseCase
    ): Camera {
        return object : Camera {
            override fun getCameraControl(): CameraControl {
                TODO("Not yet implemented")
            }

            override fun getCameraInfo(): CameraInfo {
                TODO("Not yet implemented")
            }

            override fun getCameraInternals(): LinkedHashSet<CameraInternal> {
                TODO("Not yet implemented")
            }

            override fun getExtendedConfig(): CameraConfig {
                TODO("Not yet implemented")
            }

            override fun setExtendedConfig(p0: CameraConfig?) {
                TODO("Not yet implemented")
            }
        }
    }
}
