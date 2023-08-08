package tech.relaycorp.ping.ui.common

import kotlinx.coroutines.flow.MutableSharedFlow

object Click

fun MutableSharedFlow<Click>.clicked() = this.tryEmit(Click)

object Finish
fun MutableSharedFlow<Finish>.finish() = this.tryEmit(Finish)
