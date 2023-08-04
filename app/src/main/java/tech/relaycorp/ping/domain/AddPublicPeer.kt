package tech.relaycorp.ping.domain

import kotlinx.coroutines.flow.first
import tech.relaycorp.awaladroid.endpoint.InvalidThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.ping.awala.FirstPartyEndpointLoad
import tech.relaycorp.ping.awala.OutgoingMessageBuilder
import tech.relaycorp.ping.awala.SendGatewayMessage
import tech.relaycorp.ping.data.database.dao.PublicPeerDao
import tech.relaycorp.ping.data.database.entity.PublicPeerEntity
import tech.relaycorp.ping.data.preference.AppPreferences
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

class AddPublicPeer
@Inject constructor(
    private val appPreferences: AppPreferences,
    private val publicPeerDao: PublicPeerDao,
    private val sendGatewayMessage: SendGatewayMessage,
    private val firstPartyEndpointLoad: FirstPartyEndpointLoad,
    private val outgoingMessageBuilder: OutgoingMessageBuilder,
) {

    @Throws(InvalidConnectionParams::class)
    suspend fun add(connectionParams: ByteArray): PublicThirdPartyEndpoint {
        val endpoint = try {
            PublicThirdPartyEndpoint.import(connectionParams)
        } catch (e: InvalidThirdPartyEndpoint) {
            throw InvalidConnectionParams(e)
        }

        sendAuthorisation(endpoint)

        publicPeerDao.save(
            PublicPeerEntity(
                nodeId = endpoint.nodeId,
                internetAddress = endpoint.internetAddress
            )
        )
        return endpoint
    }

    private suspend fun sendAuthorisation(grantee: PublicThirdPartyEndpoint) {
        val senderAddress = appPreferences.firstPartyEndpointAddress().first()
            ?: throw SendPingException("Sender not set")
        val sender = firstPartyEndpointLoad.load(senderAddress)
            ?: throw InvalidConnectionParams(
                InvalidThirdPartyEndpoint("Sender not registered")
            )
        val authSerialized = sender.authorizeIndefinitely(grantee)
        val outgoingAuth = outgoingMessageBuilder.build(
            AUTH_CONTENT_TYPE,
            authSerialized,
            sender,
            grantee,
            ZonedDateTime.now().plus(AUTH_PARCEL_TTL)
        )
        sendGatewayMessage.send(outgoingAuth)
    }

    companion object {
        private const val AUTH_CONTENT_TYPE = "application/vnd+relaycorp.awala.pda-path"
        private val AUTH_PARCEL_TTL = 90.days.toJavaDuration()
    }
}

class InvalidConnectionParams(cause: Throwable) : Exception(cause)
