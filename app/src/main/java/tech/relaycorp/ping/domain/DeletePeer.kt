package tech.relaycorp.ping.domain

import kotlinx.coroutines.flow.first
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.ping.data.database.dao.PublicPeerDao
import javax.inject.Inject

class DeletePeer
@Inject constructor(
    private val publicPeerDao: PublicPeerDao
) {

    suspend fun delete(nodeId: String) {
        val entity = publicPeerDao.get(nodeId).first() ?: return
        PublicThirdPartyEndpoint.load(entity.nodeId)?.delete()
        publicPeerDao.delete(entity)
    }
}
