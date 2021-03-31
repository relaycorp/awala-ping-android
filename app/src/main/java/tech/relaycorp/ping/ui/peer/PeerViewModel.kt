package tech.relaycorp.ping.ui.peer

import kotlinx.coroutines.flow.*
import tech.relaycorp.gateway.ui.BaseViewModel
import tech.relaycorp.ping.common.PublishFlow
import tech.relaycorp.ping.common.element
import tech.relaycorp.ping.domain.DeletePeer
import tech.relaycorp.ping.domain.GetPeer
import tech.relaycorp.ping.domain.model.Peer
import tech.relaycorp.ping.ui.common.Click
import tech.relaycorp.ping.ui.common.Finish
import tech.relaycorp.ping.ui.common.clicked
import tech.relaycorp.ping.ui.common.finish
import java.util.*
import javax.inject.Inject

class PeerViewModel
@Inject constructor(
    private val getPeer: GetPeer,
    private val deletePeer: DeletePeer
) : BaseViewModel() {

    // Inputs

    private val privateAddressReceived = MutableStateFlow(Optional.empty<String>())
    fun privateAddressReceived(value: String) {
        privateAddressReceived.value = Optional.of(value)
    }

    private val deleteClicks = PublishFlow<Click>()
    fun deleteClicked() = deleteClicks.clicked()

    // Outputs

    private val _peer = MutableStateFlow(Optional.empty<Peer>())
    fun peer() = _peer.asStateFlow().element()

    private val _finish = PublishFlow<Finish>()
    fun finish() = _finish.asFlow()

    init {
        privateAddressReceived
            .element()
            .flatMapLatest(getPeer::get)
            .onEach { _peer.value = Optional.of(it) }
            .launchIn(backgroundScope)

        deleteClicks
            .asFlow()
            .flatMapLatest { privateAddressReceived.element() }
            .onEach {
                deletePeer.delete(it)
                _finish.finish()
            }
            .launchIn(backgroundScope)
    }
}
