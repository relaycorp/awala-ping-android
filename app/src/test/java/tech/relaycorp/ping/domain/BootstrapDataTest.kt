package tech.relaycorp.ping.domain

import android.content.res.Resources
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.verifyNoInteractions
import tech.relaycorp.awaladroid.GatewayProtocolException
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.ping.awala.FirstPartyEndpointRegistration
import tech.relaycorp.ping.data.preference.AppPreferences

class BootstrapDataTest {

    private val resources = mock<Resources>()
    private val appPreferences = mock<AppPreferences>()
    private val addPublicPeer = mock<AddPublicPeer>()
    private val registerFirstPartyEndpoint = mock<FirstPartyEndpointRegistration>()

    private val subject =
        BootstrapData(resources, appPreferences, addPublicPeer, registerFirstPartyEndpoint)

    @Test
    fun bootstrap_ifNotNeeded() = runBlockingTest {
        whenever(appPreferences.firstPartyEndpointAddress()).thenReturn(flowOf("123456"))

        val success = subject.bootstrapIfNeeded()

        assert(success)
        verifyNoInteractions(registerFirstPartyEndpoint)
        verifyNoInteractions(addPublicPeer)
        verify(appPreferences, never()).setFirstPartyEndpointAddress(any())
    }

    @Test
    fun bootstrap_ifNeeded() = runBlockingTest {
        whenever(appPreferences.firstPartyEndpointAddress()).thenReturn(flowOf(null))
        val firstPartyEndpoint = mock<FirstPartyEndpoint>()
        val firstPartyAddress = "123456"
        whenever(firstPartyEndpoint.nodeId).thenReturn(firstPartyAddress)
        whenever(registerFirstPartyEndpoint.register()).thenReturn(firstPartyEndpoint)
        whenever(resources.openRawResource(any())).thenReturn(ByteArray(0).inputStream())
        whenever(appPreferences.setFirstPartyEndpointAddress(firstPartyAddress)).thenReturn(true)

        val success = subject.bootstrapIfNeeded()

        assert(success)
        verify(addPublicPeer).add(any())
        verify(registerFirstPartyEndpoint).register()
        verify(appPreferences).setFirstPartyEndpointAddress(eq(firstPartyAddress))
    }

    @Test
    fun boostrap_registerException() = runTest {
        whenever(appPreferences.firstPartyEndpointAddress()).thenReturn(flowOf(null))
        whenever(resources.openRawResource(any())).thenReturn(ByteArray(0).inputStream())
        given(registerFirstPartyEndpoint.register()).willAnswer {
            throw GatewayProtocolException("")
        }


        val error = subject.bootstrapIfNeeded()

        assert(!error)
        verify(addPublicPeer).add(any())
        verify(registerFirstPartyEndpoint).register()
        verify(appPreferences, never()).setFirstPartyEndpointAddress(any())
    }
}
