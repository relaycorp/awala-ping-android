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
import tech.relaycorp.awaladroid.messaging.ParcelId
import tech.relaycorp.ping.awala.*
import tech.relaycorp.ping.data.database.dao.PingDao
import tech.relaycorp.ping.data.preference.AppPreferences
import tech.relaycorp.ping.domain.model.Peer
import tech.relaycorp.ping.domain.model.PeerType
import tech.relaycorp.ping.test.PublicPeerEntityFactory
import tech.relaycorp.relaynet.pki.CertificationPath
import tech.relaycorp.relaynet.testing.pki.KeyPairSet
import tech.relaycorp.relaynet.testing.pki.PDACertPath
import tech.relaycorp.relaynet.wrappers.nodeId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
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
        val senderAddress = KeyPairSet.PRIVATE_ENDPOINT.public.nodeId
        whenever(appPreferences.firstPartyEndpointAddress()).thenReturn(flowOf(senderAddress))
        val sender = mock<FirstPartyEndpoint>()
        val internetAddress = "frankfurt.relaycorp.cloud"
        whenever(sender.internetAddress).thenReturn(internetAddress)
        whenever(firstPartyEndpointLoad.load(any())).thenReturn(sender)
        val recipient = mock<PublicThirdPartyEndpoint>()
        val internetRecipientAddress = "example.org"
        whenever(recipient.internetAddress).thenReturn(internetRecipientAddress)
        whenever(publicThirdPartyEndpointLoad.load(any())).thenReturn(recipient)

        whenever(sender.issueAuthorization(any<ThirdPartyEndpoint>(), any())).thenReturn(
            CertificationPath(
                PDACertPath.PRIVATE_GW,
                emptyList()
            ).serialize()
        )

        val pingMessageSerialized = ByteArray(0)
        whenever(pingSerialization.serialize(any(), any(), eq(internetAddress)))
            .thenReturn(pingMessageSerialized)

        val outgoingMessage = mock<OutgoingMessage>()
        val parcelId = ParcelId.generate()
        whenever(outgoingMessage.parcelId).thenReturn(parcelId)
        whenever(outgoingMessageBuilder.build(any(), any(), any(), any(), any()))
            .thenReturn(outgoingMessage)

        val peerEntity = PublicPeerEntityFactory.build()
        val peer = Peer(peerEntity.nodeId, peerEntity.internetAddress, PeerType.Public)
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
            assertEquals(peer.nodeId, it.peerId)
            assertEquals(parcelId.value, it.parcelId)
            assertEquals(peer.peerType, it.peerType)
            val expiresAtDiff = ChronoUnit.MINUTES.between(ZonedDateTime.now(), it.expiresAt)
            assertTrue(expiresAtDiff in 4..6)
        })
    }
}
