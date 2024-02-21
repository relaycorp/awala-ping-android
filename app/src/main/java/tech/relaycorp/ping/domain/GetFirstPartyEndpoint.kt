package tech.relaycorp.ping.domain

import kotlinx.coroutines.flow.first
import tech.relaycorp.ping.awala.FirstPartyEndpointLoad
import tech.relaycorp.ping.data.preference.AppPreferences
import javax.inject.Inject

class GetFirstPartyEndpoint @Inject constructor(
    private val appPreferences: AppPreferences,
    private val firstPartyEndpointLoad: FirstPartyEndpointLoad,
) {
    suspend operator fun invoke() =
        appPreferences.firstPartyEndpointAddress().first()
            ?.let { senderAddress -> firstPartyEndpointLoad.load(senderAddress) }
}