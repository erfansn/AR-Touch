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

package ir.erfansn.artouch.dispatcher.ble

import android.bluetooth.BluetoothHidDevice
import android.os.ParcelUuid
import java.util.UUID

internal object ArTouchSpecification {

    // Formatted in SIG-adopted base UUID 0000xxxx-0000-1000-8000-00805F9B34FB.
    val SERVICE_UUID = ParcelUuid(UUID.fromString("00004397-0000-1000-8000-00805F9B34FB"))

    const val NAME = "AR Touch"
    const val DESCRIPTION = "Add touch functionality to any surface of any size"
    const val PROVIDER = "Erfan Sn"
    const val SUBCLASS = BluetoothHidDevice.SUBCLASS2_DIGITIZER_TABLET

    const val REPORT_ID: UByte = 0x01u
    @OptIn(ExperimentalUnsignedTypes::class)
    val REPORT_DESCRIPTOR = ubyteArrayOf(
        0x05u, 0x0Du,                    // USAGE_PAGE(Digitizers)
        0x09u, 0x04u,                    // USAGE     (Touch Screen)
        0xA1u, 0x01u,                    // COLLECTION(Application)
        0x85u, REPORT_ID,                //   REPORT_ID (Touch)

        // define the maximum amount of fingers that the device supports
        0x09u, 0x55u,                    //   USAGE (Contact Count Maximum)
        0x25u, 0X01u,                    //   LOGICAL_MAXIMUM (CONTACT_COUNT_MAXIMUM)
        0xB1u, 0x02u,                    //   FEATURE (Data,Var,Abs)

        // define the actual amount of fingers that are concurrently touching the screen
        0x09u, 0x54u,                    //   USAGE (Contact count)
        0x95u, 0x01u,                    //   REPORT_COUNT(1)
        0x75u, 0x08u,                    //   REPORT_SIZE (8)
        0x81u, 0x02u,                    //   INPUT (Data,Var,Abs)

        // declare a finger collection
        0x09u, 0x22u,                    //   USAGE (Finger)
        0xA1u, 0x02u,                    //   COLLECTION (Logical)

        // declare an identifier for the finger
        0x09u, 0x51u,                    //     USAGE (Contact Identifier)
        0x75u, 0x08u,                    //     REPORT_SIZE (8)
        0x95u, 0x01u,                    //     REPORT_COUNT (1)
        0x81u, 0x02u,                    //     INPUT (Data,Var,Abs)

        // declare Tip Switch and In Range
        0x09u, 0x42u,                    //     USAGE (Tip Switch)
        0x09u, 0x32u,                    //     USAGE (In Range)
        0x15u, 0x00u,                    //     LOGICAL_MINIMUM (0)
        0x25u, 0x01u,                    //     LOGICAL_MAXIMUM (1)
        0x75u, 0x01u,                    //     REPORT_SIZE (1)
        0x95u, 0x02u,                    //     REPORT_COUNT(2)
        0x81u, 0x02u,                    //     INPUT (Data,Var,Abs)

        // declare the remaining 6 bits of the first data byte as constant -> the driver will ignore them
        0x95u, 0x06u,                    //     REPORT_COUNT (6)
        0x81u, 0x03u,                    //     INPUT (Cnst,Ary,Abs)

        // define absolute X and Y coordinates of 16 bit each (percent values multiplied with 100)
        0x05u, 0x01u,                    //     USAGE_PAGE (Generic Desktop)
        0x09u, 0x30u,                    //     Usage (X)
        0x09u, 0x31u,                    //     Usage (Y)
        0x16u, 0x00u, 0x00u,             //     Logical Minimum (0)
        0x26u, 0x10u, 0x27u,             //     Logical Maximum (10000)
        0x36u, 0x00u, 0x00u,             //     Physical Minimum (0)
        0x46u, 0x10u, 0x27u,             //     Physical Maximum (10000)
        0x66u, 0x00u, 0x00u,             //     UNIT (None)
        0x75u, 0x10u,                    //     Report Size (16),
        0x95u, 0x02u,                    //     Report Count (2),
        0x81u, 0x02u,                    //     Input (Data,Var,Abs)
        0xC0u,                           //   END_COLLECTION
        0xC0u                            // END_COLLECTION

        // with this declaration a data packet must be sent as:
        // byte 1   -> "contact count"        (always == 1)
        // byte 2   -> "contact identifier"   (any value)
        // byte 3   -> "Tip Switch" state     (bit 0 = Tip Switch up/down, bit 1 = In Range)
        // byte 4,5 -> absolute X coordinate  (0...10000)
        // byte 6,7 -> absolute Y coordinate  (0...10000)
    ).toByteArray()
}
