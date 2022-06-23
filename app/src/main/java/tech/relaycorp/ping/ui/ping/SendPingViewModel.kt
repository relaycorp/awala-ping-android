package tech.relaycorp.ping.ui.ping

import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.relaycorp.ping.ui.BaseViewModel
import tech.relaycorp.ping.common.Logging.logger
import tech.relaycorp.ping.common.PublishFlow
import tech.relaycorp.ping.domain.GetDefaultPeer
import tech.relaycorp.ping.domain.SendPing
import tech.relaycorp.ping.domain.model.Peer
import tech.relaycorp.ping.ui.common.Click
import tech.relaycorp.ping.ui.common.clicked
import java.util.*
import java.util.logging.Level
import javax.inject.Inject

class SendPingViewModel
@Inject constructor(
    private val getDefaultPeer: GetDefaultPeer,
    private val sendPing: SendPing
) : BaseViewModel() {

    // Inputs

    private val peerPicks = MutableStateFlow(Optional.empty<Peer>())
    fun peerPicked(value: Peer) {
        peerPicks.value = Optional.of(value)
    }

    private val expiresAtChanges = MutableStateFlow(ExpireDuration.DEFAULT)
    fun expiresAtChanged(value: ExpireDuration) {
        expiresAtChanges.value = value
    }

    private val sendClicks = PublishFlow<Click>()
    fun sendClicked() = sendClicks.clicked()

    // Outputs

    fun peer(): Flow<Optional<Peer>> = peerPicks.asStateFlow()
    fun expiresAt(): Flow<ExpireDuration> = expiresAtChanges.asStateFlow()

    private val _sendEnabled = MutableStateFlow(false)
    fun sendEnabled(): Flow<Boolean> = _sendEnabled.asStateFlow()

    private val _sending = MutableStateFlow(false)
    fun sending(): Flow<Boolean> = _sending.asStateFlow()

    private val _errors = PublishFlow<Error>()
    fun errors() = _errors.asFlow()

    private val _finishToPing = PublishFlow<String>()
    fun finishToPing() = _finishToPing.asFlow()

    init {
        peerPicks
            .onEach { _sendEnabled.value = it.isPresent }
            .launchIn(backgroundScope)

        sendClicks
            .asFlow()
            .filter { _sendEnabled.value }
            .onEach {
                _sendEnabled.value = false
                _sending.value = true
                val pingId = sendPing.send(
                    peerPicks.value.get(),
                    expiresAtChanges.value.toKotlinDuration()
                )
                _sending.value = false
                _finishToPing.trySendBlocking(pingId)
            }
            .catch { exp ->
                logger.log(Level.WARNING, "Error sending ping", exp)
                _sending.value = false
                _sendEnabled.value = true
                _errors.send(Error.Sending)
            }
            .launchIn(backgroundScope)

        backgroundScope.launch {
            getDefaultPeer.get()?.let { peerPicked(it) }
        }
    }

    enum class Error {
        Sending
    }
}
