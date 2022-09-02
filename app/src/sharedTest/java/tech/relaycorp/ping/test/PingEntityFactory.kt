package tech.relaycorp.ping.test

import tech.relaycorp.ping.data.database.entity.PingEntity
import tech.relaycorp.ping.data.database.entity.PublicPeerEntity
import tech.relaycorp.ping.domain.model.PeerType
import java.time.ZonedDateTime
import java.util.*

object PingEntityFactory {
    fun build(peer: PublicPeerEntity): PingEntity {
        val now = ZonedDateTime.now().withNano(0)
        return PingEntity(
            pingId = UUID.randomUUID().toString(),
            peerId = peer.nodeId,
            peerType = PeerType.Public,
            sentAt = now,
            expiresAt = now.plusDays(1)
        )
    }
}
