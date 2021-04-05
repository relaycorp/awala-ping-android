package tech.relaycorp.ping.common

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.time.Duration

fun <T> PublishFlow() = BroadcastChannel<T>(1)

fun <T> Flow<Optional<T>>.element(): Flow<T> =
    flatMapLatest {
        if (it.isPresent) flowOf(it.get()!!) else emptyFlow()
    }

fun flowInterval(repeatMillis: Duration) =
    flow {
        while (true) {
            emit(Unit)
            delay(repeatMillis)
        }
    }
