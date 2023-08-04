package tech.relaycorp.ping.domain

import kotlinx.coroutines.flow.first
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.ping.awala.AwalaPing
import tech.relaycorp.ping.common.Logging.logger
import tech.relaycorp.ping.data.database.dao.PingDao
import java.time.ZonedDateTime
import java.util.logging.Level
import javax.inject.Inject

class ReceivePong
@Inject constructor(
    private val pingDao: PingDao
) {

    suspend fun receive(incomingMessage: IncomingMessage) {
        if (incomingMessage.type != AwalaPing.V1.PongType) {
            incomingMessage.ack()
            return
        }

        val pingId = incomingMessage.content.decodeToString()
        val ping = pingDao.getPublic(pingId).first()?.ping ?: run {
            logger.log(Level.INFO, "Received pong for unknown ping $pingId")
            incomingMessage.ack()
            return@receive
        }

        if (ping.pongReceivedAt == null) {
            pingDao.save(
                ping.copy(pongReceivedAt = ZonedDateTime.now())
            )
        }

        incomingMessage.ack()
    }
}
