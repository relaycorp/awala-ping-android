package tech.relaycorp.ping.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Peer(
    val nodeId: String,
    val alias: String,
    val peerType: PeerType
) : Parcelable
