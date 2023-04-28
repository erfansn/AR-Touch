package ir.erfansn.artouch

import android.content.SharedPreferences
import androidx.core.content.edit

operator fun SharedPreferences.set(key: String, value: Boolean) = edit {
    putBoolean(key, value)
}

operator fun SharedPreferences.get(key: String, default: Boolean = false) =
    getBoolean(key, default)
