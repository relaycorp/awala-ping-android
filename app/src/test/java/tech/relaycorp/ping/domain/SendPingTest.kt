package tech.relaycorp.ping.domain

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.ThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.ping.awala.*
import tech.relaycorp.ping.data.database.dao.PingDao
import tech.relaycorp.ping.data.preference.AppPreferences
import tech.relaycorp.ping.domain.model.Peer
import tech.relaycorp.ping.domain.model.PeerType
import tech.relaycorp.ping.test.PublicPeerEntityFactory
import tech.relaycorp.relaynet.pki.CertificationPath
import tech.relaycorp.relaynet.testing.pki.KeyPairSet
import tech.relaycorp.relaynet.testing.pki.PDACertPath
import tech.relaycorp.relaynet.wrappers.privateAddress
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.minutes

class SendPingTest {

    private val appPreferences = mock<AppPreferences>()
    private val firstPartyEndpointLoad = mock<FirstPartyEndpointLoad>()
    private val publicThirdPartyEndpointLoad = mock<PublicThirdPartyEndpointLoad>()
    private val sendGatewayMessage = mock<SendGatewayMessage>()
    private val pingSerialization = mock<PingSerialization>()
    private val outgoingMessageBuilder = mock<OutgoingMessageBuilder>()
    private val pingDao = mock<PingDao>()
    private val subject = SendPing(
        appPreferences,
        firstPartyEndpointLoad,
        publicThirdPartyEndpointLoad,
        sendGatewayMessage,
        pingSerialization,
        outgoingMessageBuilder,
        pingDao
    )

    @Test
    fun sendSuccessful() = runBlockingTest {
        val senderAddress = KeyPairSet.PRIVATE_ENDPOINT.public.privateAddress
        whenever(appPreferences.firstPartyEndpointAddress()).thenReturn(flowOf(senderAddress))
        val sender = mock<FirstPartyEndpoint>()
        whenever(firstPartyEndpointLoad.load(any())).thenReturn(sender)
        val recipient = mock<PublicThirdPartyEndpoint>()
        whenever(publicThirdPartyEndpointLoad.load(any())).thenReturn(recipient)

        whenever(sender.issueAuthorization(any<ThirdPartyEndpoint>(), any())).thenReturn(
            CertificationPath(
                PDACertPath.PRIVATE_GW,
                emptyList()
            ).serialize()
        )

        val pingMessageSerialized = ByteArray(0)
        whenever(pingSerialization.serialize(any(), any()))
            .thenReturn(pingMessageSerialized)

        val outgoingMessage = mock<OutgoingMessage>()
        whenever(outgoingMessageBuilder.build(any(), any(), any(), any(), any()))
            .thenReturn(outgoingMessage)

        val peerEntity = PublicPeerEntityFactory.build()
        val peer = Peer(peerEntity.privateAddress, peerEntity.publicAddress, PeerType.Public)
        val duration = 5.minutes

        subject.send(peer, duration)

        verify(outgoingMessageBuilder).build(
            eq(AwalaPing.V1.PingType),
            eq(pingMessageSerialized),
            eq(sender),
            eq(recipient),
            check {
                val diff = ChronoUnit.MINUTES.between(ZonedDateTime.now(), it)
                assertTrue(diff in 4..6)
            }
        )
        verify(sendGatewayMessage).send(outgoingMessage)
        verify(pingDao).save(check {
            assertEquals(peer.privateAddress, it.peerPrivateAddress)
            assertEquals(peer.peerType, it.peerType)
            val expiresAtDiff = ChronoUnit.MINUTES.between(ZonedDateTime.now(), it.expiresAt)
            assertTrue(expiresAtDiff in 4..6)
        })
    }
}
