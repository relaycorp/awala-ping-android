package tech.relaycorp.ping.domain

import tech.relaycorp.awaladroid.endpoint.InvalidThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.ping.data.database.dao.PublicPeerDao
import tech.relaycorp.ping.data.database.entity.PublicPeerEntity
import javax.inject.Inject

class AddPublicPeer
@Inject constructor(
    private val publicPeerDao: PublicPeerDao
) {

    @Throws(InvalidConnectionParams::class)
    suspend fun add(connectionParams: ByteArray): PublicThirdPartyEndpoint {
        val endpoint = try {
            PublicThirdPartyEndpoint.import(connectionParams)
        } catch (e: InvalidThirdPartyEndpoint) {
            throw InvalidConnectionParams(e)
        }
        publicPeerDao.save(
            PublicPeerEntity(
                privateAddress = endpoint.privateAddress,
                publicAddress = endpoint.publicAddress
            )
        )
        return endpoint
    }
}

class InvalidConnectionParams(cause: Throwable) : Exception(cause)
