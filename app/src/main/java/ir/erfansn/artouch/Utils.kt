package ir.erfansn.artouch

import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Window.enableImmersiveMode() {
    WindowCompat.getInsetsController(this, decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hide(WindowInsetsCompat.Type.systemBars())
    }
}

fun Window.disableImmersiveMode() {
    WindowCompat.getInsetsController(this, decorView).apply {
        show(WindowInsetsCompat.Type.systemBars())
    }
}
