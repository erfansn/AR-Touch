package ir.erfansn.artouch

import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Window.disableImmersiveMode() {
    WindowCompat.getInsetsController(this, decorView).apply {
        show(WindowInsetsCompat.Type.systemBars())
    }
}
