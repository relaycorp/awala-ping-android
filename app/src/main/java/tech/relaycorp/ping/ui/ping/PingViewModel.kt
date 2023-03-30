package tech.relaycorp.ping.ui.ping

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import tech.relaycorp.ping.common.element
import tech.relaycorp.ping.common.flowInterval
import tech.relaycorp.ping.domain.GetPing
import tech.relaycorp.ping.domain.model.Ping
import tech.relaycorp.ping.ui.BaseViewModel
import java.util.Optional
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class PingViewModel
@Inject constructor(
    private val getPing: GetPing
) : BaseViewModel() {

    private val pingId = MutableStateFlow(Optional.empty<String>())
    fun pingIdReceived(value: String) {
        pingId.value = Optional.of(value)
    }

    private val _ping = MutableStateFlow(Optional.empty<Ping>())
    fun ping() = _ping.asStateFlow().element()

    init {
        pingId
            .element()
            .flatMapLatest { pingId -> flowInterval(10.seconds).map { pingId } } // Refresh display
            .flatMapLatest(getPing::get)
            .onEach { _ping.value = Optional.of(it) }
            .launchIn(backgroundScope)
    }
}
