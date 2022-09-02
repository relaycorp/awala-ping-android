package tech.relaycorp.ping.awala

import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint

interface PublicThirdPartyEndpointLoad {
    suspend fun load(nodeId: String): PublicThirdPartyEndpoint?
}
