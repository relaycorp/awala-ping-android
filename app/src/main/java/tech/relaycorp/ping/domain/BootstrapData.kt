package tech.relaycorp.ping.domain

import android.content.res.Resources
import kotlinx.coroutines.flow.first
import tech.relaycorp.awaladroid.GatewayProtocolException
import tech.relaycorp.ping.R
import tech.relaycorp.ping.awala.FirstPartyEndpointRegistration
import tech.relaycorp.ping.common.Logging.logger
import tech.relaycorp.ping.data.preference.AppPreferences
import java.util.logging.Level
import javax.inject.Inject

class BootstrapData
@Inject constructor(
    private val resources: Resources,
    private val appPreferences: AppPreferences,
    private val addPublicPeer: AddPublicPeer,
    private val firstPartyEndpointRegistration: FirstPartyEndpointRegistration
) {

    suspend fun bootstrapIfNeeded(): Boolean {
        try {
            if (appPreferences.firstPartyEndpointAddress().first() != null) {
                return true
            }

            importDefaultPublicPeer()
            val endpoint = firstPartyEndpointRegistration.register()
            return appPreferences.setFirstPartyEndpointAddress(endpoint.nodeId)
        } catch (exp: GatewayProtocolException) {
            logger.log(Level.WARNING, "Gateway Protocol exception", exp)
            return false
        }
    }

    private suspend fun importDefaultPublicPeer() {
        addPublicPeer.add(
            resources.openRawResource(R.raw.default_public_peer_connection_params).use {
                it.readBytes()
            }
        )
    }
}
