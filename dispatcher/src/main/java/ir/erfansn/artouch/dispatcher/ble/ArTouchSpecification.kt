package ir.erfansn.artouch.dispatcher.ble

import android.os.ParcelUuid
import java.util.UUID

internal object ArTouchSpecification {

    // Formatted in SIG-adopted base UUID 0000xxxx-0000-1000-8000-00805F9B34FB.
    val SERVICE_UUID = ParcelUuid(UUID.fromString("00004397-0000-1000-8000-00805F9B34FB"))
}
