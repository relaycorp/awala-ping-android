package tech.relaycorp.ping.test

import tech.relaycorp.ping.data.database.entity.PublicPeerEntity
import java.util.*

object PublicPeerEntityFactory {
    fun build() = PublicPeerEntity(
        nodeId = UUID.randomUUID().toString(),
        internetAddress = "example.org"
    )
}
