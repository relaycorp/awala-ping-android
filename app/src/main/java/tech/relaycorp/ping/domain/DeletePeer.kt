package tech.relaycorp.ping.domain

import kotlinx.coroutines.flow.first
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.ping.data.database.dao.PublicPeerDao
import javax.inject.Inject

class DeletePeer
@Inject constructor(
    private val publicPeerDao: PublicPeerDao,
    private val getFirstPartyEndpoint: GetFirstPartyEndpoint
) {

    suspend fun delete(nodeId: String) {
        val entity = publicPeerDao.get(nodeId).first() ?: return
        val firstParty = getFirstPartyEndpoint() ?: return
        PublicThirdPartyEndpoint.load(entity.nodeId)?.delete(firstParty)
        publicPeerDao.delete(entity)
    }
}
