package tech.relaycorp.ping.domain.model

import java.time.ZonedDateTime

data class Ping(
    val pingId: String,
    val parcelId: String,
    val peer: Peer,
    val sentAt: ZonedDateTime,
    val expiresAt: ZonedDateTime,
    val pongReceivedAt: ZonedDateTime? = null
) {

    val state
        get() = when {
            pongReceivedAt != null -> State.SendAndReplied
            expiresAt < ZonedDateTime.now() -> State.Expired
            else -> State.Sent
        }

    enum class State {
        Sent, SendAndReplied, Expired
    }
}
