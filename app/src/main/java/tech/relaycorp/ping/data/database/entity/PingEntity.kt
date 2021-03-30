package tech.relaycorp.ping.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import tech.relaycorp.ping.domain.model.PeerType
import tech.relaycorp.ping.domain.model.Ping
import java.time.ZonedDateTime

@Entity(tableName = "ping")
data class PingEntity(
    @PrimaryKey val pingId: String,
    val peerPrivateAddress: String,
    val peerType: PeerType,
    val sentAt: ZonedDateTime,
    val expiresAt: ZonedDateTime,
    val pongReceivedAt: ZonedDateTime? = null
)

data class PingWithPublicPeer(
    @Embedded val ping: PingEntity,
    @Relation(
        parentColumn = "peerPrivateAddress",
        entityColumn = "privateAddress"
    ) val publicPeer: PublicPeerEntity
) {
    fun toModel() = Ping(
        pingId = ping.pingId,
        peer = publicPeer.toModel(),
        sentAt = ping.sentAt,
        expiresAt = ping.expiresAt,
        pongReceivedAt = ping.pongReceivedAt
    )
}
