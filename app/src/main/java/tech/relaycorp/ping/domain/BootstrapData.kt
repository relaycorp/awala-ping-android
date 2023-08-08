package tech.relaycorp.ping.domain

import android.content.res.Resources
import kotlinx.coroutines.flow.first
import tech.relaycorp.ping.R
import tech.relaycorp.ping.awala.FirstPartyEndpointRegistration
import tech.relaycorp.ping.data.preference.AppPreferences
import javax.inject.Inject

class BootstrapData
@Inject constructor(
    private val resources: Resources,
    private val appPreferences: AppPreferences,
    private val addPublicPeer: AddPublicPeer,
    private val firstPartyEndpointRegistration: FirstPartyEndpointRegistration
) {

    suspend fun bootstrapIfNeeded() {
        if (appPreferences.firstPartyEndpointAddress().first() != null) return

        val endpoint = firstPartyEndpointRegistration.register()
        appPreferences.setFirstPartyEndpointAddress(endpoint.nodeId)
        importDefaultPublicPeer()
    }

    private suspend fun importDefaultPublicPeer() {
        addPublicPeer.add(
            resources.openRawResource(R.raw.default_public_peer_connection_params).use {
                it.readBytes()
            }
        )
    }
}
