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
