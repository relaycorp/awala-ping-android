package tech.relaycorp.ping.ui.common

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.trySendBlocking

object Click

fun BroadcastChannel<Click>.clicked() = this.trySendBlocking(Click)

object Finish
fun BroadcastChannel<Finish>.finish() = this.trySendBlocking(Finish)
