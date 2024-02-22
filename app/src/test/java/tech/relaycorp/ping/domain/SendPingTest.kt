package tech.relaycorp.ping.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.awaladroid.messaging.ParcelId
import tech.relaycorp.ping.awala.AwalaPing
import tech.relaycorp.ping.awala.FirstPartyEndpointLoad
import tech.relaycorp.ping.awala.OutgoingMessageBuilder
import tech.relaycorp.ping.awala.PublicThirdPartyEndpointLoad
import tech.relaycorp.ping.awala.SendGatewayMessage
import tech.relaycorp.ping.data.database.dao.PingDao
import tech.relaycorp.ping.data.preference.AppPreferences
import tech.relaycorp.ping.domain.model.Peer
import tech.relaycorp.ping.domain.model.PeerType
import tech.relaycorp.ping.test.PublicPeerEntityFactory
import tech.relaycorp.relaynet.testing.pki.KeyPairSet
import tech.relaycorp.relaynet.wrappers.nodeId
import java.nio.charset.Charset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class SendPingTest {

    private val appPreferences = mock<AppPreferences>()
    private val getFirstPartyEndpoint = mock<GetFirstPartyEndpoint>()
    private val publicThirdPartyEndpointLoad = mock<PublicThirdPartyEndpointLoad>()
    private val sendGatewayMessage = mock<SendGatewayMessage>()
    private val outgoingMessageBuilder = mock<OutgoingMessageBuilder>()
    private val pingDao = mock<PingDao>()
    private val subject = SendPing(
        appPreferences,
        getFirstPartyEndpoint,
        publicThirdPartyEndpointLoad,
        sendGatewayMessage,
        outgoingMessageBuilder,
        pingDao
    )

    @Test
    fun sendSuccessful() = runTest {
        val senderAddress = KeyPairSet.PRIVATE_ENDPOINT.public.nodeId
        whenever(appPreferences.firstPartyEndpointAddress()).thenReturn(flowOf(senderAddress))
        val sender = mock<FirstPartyEndpoint>()
        whenever(getFirstPartyEndpoint()).thenReturn(sender)
        val recipient = mock<PublicThirdPartyEndpoint>()
        whenever(publicThirdPartyEndpointLoad.load(any())).thenReturn(recipient)

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
            check {
                val pingId = it.toString(Charset.defaultCharset())
                val uuid = UUID.fromString(pingId)
                assertEquals(4, uuid.version())
            },
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
