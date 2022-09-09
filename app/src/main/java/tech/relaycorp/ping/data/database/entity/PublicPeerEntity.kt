package tech.relaycorp.ping.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import tech.relaycorp.ping.domain.model.Peer
import tech.relaycorp.ping.domain.model.PeerType

@Entity(tableName = "public_peer")
data class PublicPeerEntity(
    @PrimaryKey val nodeId: String,
    val internetAddress: String,
    val deleted: Boolean = false
) {

    fun toModel() = Peer(
        nodeId = nodeId,
        alias = internetAddress,
        peerType = PeerType.Public
    )
}
