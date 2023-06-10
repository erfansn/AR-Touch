package ir.erfansn.artouch.producer

import kotlinx.coroutines.flow.Flow

interface TouchEventProducer {
    val touchEvent: Flow<TouchEvent>
}
