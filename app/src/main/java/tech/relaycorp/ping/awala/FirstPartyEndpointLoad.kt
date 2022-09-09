package tech.relaycorp.ping.awala

import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint

interface FirstPartyEndpointLoad {
    suspend fun load(nodeId: String): FirstPartyEndpoint?
}
