package tech.relaycorp.ping.domain

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.ping.awala.AwalaPing
import tech.relaycorp.ping.awala.PingSerialization
import tech.relaycorp.ping.data.database.dao.PingDao
import tech.relaycorp.ping.data.database.entity.PingWithPublicPeer
import tech.relaycorp.ping.test.PingEntityFactory
import tech.relaycorp.ping.test.PublicPeerEntityFactory
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class ReceivePongTest {

    private val pingDao = mock<PingDao>()
    private val pingSerialization = mock<PingSerialization>()
    private val receivePong = ReceivePong(pingDao, pingSerialization)

    @Test
    fun receiveSuccessful() = runBlockingTest {
        val message = mock<IncomingMessage>()
        whenever(message.type).thenReturn(AwalaPing.V1.PongType)
        whenever(message.content).thenReturn(ByteArray(0))
        whenever(message.ack).thenReturn(suspend {})

        val pingId = "12345"
        whenever(pingSerialization.extractPingIdFromPong(any())).thenReturn(pingId)

        val peer = PublicPeerEntityFactory.build()
        val ping = PingEntityFactory.build(peer)
        whenever(pingDao.getPublic(pingId))
            .thenReturn(flowOf(PingWithPublicPeer(ping, peer)))

        receivePong.receive(message)

        verify(pingDao).save(check {
            val diff = ChronoUnit.SECONDS.between(ZonedDateTime.now(), it.pongReceivedAt)
            assertTrue(diff in 0..1)
        })
        verify(message).ack
    }
}
