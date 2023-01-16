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
    val parcelId: String,
    val peerId: String,
    val peerType: PeerType,
    val sentAt: ZonedDateTime,
    val expiresAt: ZonedDateTime,
    val pongReceivedAt: ZonedDateTime? = null
)

data class PingWithPublicPeer(
    @Embedded val ping: PingEntity,
    @Relation(
        parentColumn = "peerId",
        entityColumn = "nodeId"
    ) val publicPeer: PublicPeerEntity
) {
    fun toModel() = Ping(
        pingId = ping.pingId,
        parcelId = ping.parcelId,
        peer = publicPeer.toModel(),
        sentAt = ping.sentAt,
        expiresAt = ping.expiresAt,
        pongReceivedAt = ping.pongReceivedAt
    )
}
