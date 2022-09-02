package tech.relaycorp.ping.awala

import dagger.Module
import dagger.Provides
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.ThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import java.time.ZonedDateTime

@Module
class AwalaModule {

    @Provides
    fun firstPartyEndpointRegistration(): FirstPartyEndpointRegistration =
        object : FirstPartyEndpointRegistration {
            override suspend fun register(): FirstPartyEndpoint =
                FirstPartyEndpoint.register()
        }

    @Provides
    fun firstPartyEndpointLoad(): FirstPartyEndpointLoad =
        object : FirstPartyEndpointLoad {
            override suspend fun load(nodeId: String): FirstPartyEndpoint? =
                FirstPartyEndpoint.load(nodeId)
        }

    @Provides
    fun publicThirdPartyEndpointLoad(): PublicThirdPartyEndpointLoad =
        object : PublicThirdPartyEndpointLoad {
            override suspend fun load(nodeId: String): PublicThirdPartyEndpoint? =
                PublicThirdPartyEndpoint.load(nodeId)
        }

    @Provides
    fun OutgoingMessageBuilder(): OutgoingMessageBuilder =
        object : OutgoingMessageBuilder {
            override suspend fun build(
                type: String,
                content: ByteArray,
                senderEndpoint: FirstPartyEndpoint,
                recipientEndpoint: ThirdPartyEndpoint,
                parcelExpiryDate: ZonedDateTime
            ): OutgoingMessage =
                OutgoingMessage.build(
                    type, content, senderEndpoint, recipientEndpoint, parcelExpiryDate
                )
        }

    @Provides
    fun sendGatewayMessage(): SendGatewayMessage = object : SendGatewayMessage {
        override suspend fun send(outgoingMessage: OutgoingMessage) {
            GatewayClient.sendMessage(outgoingMessage)
        }
    }
}
