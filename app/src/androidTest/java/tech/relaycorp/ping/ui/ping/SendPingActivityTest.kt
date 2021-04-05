package tech.relaycorp.ping.ui.ping

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions.assertDisabled
import com.schibsted.spain.barista.assertion.BaristaEnabledAssertions.assertEnabled
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tech.relaycorp.ping.R
import tech.relaycorp.ping.data.database.dao.PublicPeerDao
import tech.relaycorp.ping.test.AppTestProvider.component
import tech.relaycorp.ping.test.AppTestProvider.context
import tech.relaycorp.ping.test.BaseActivityTestRule
import tech.relaycorp.ping.test.PublicPeerEntityFactory
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class SendPingActivityTest {

    @Rule
    @JvmField
    val testRule = BaseActivityTestRule(SendPingActivity::class, false)

    @Inject
    lateinit var publicPeerDao: PublicPeerDao

    @Before
    fun setUp() {
        component.inject(this)
    }

    @Test
    fun saveEnableState() {
        testRule.start(SendPingActivity.getIntent(context))

        assertDisabled(R.id.send)

        val peer = PublicPeerEntityFactory.build()
        runBlocking {
            publicPeerDao.save(peer)
        }

        clickOn(R.id.toButton)
        clickOn(peer.publicAddress)

        assertEnabled(R.id.send)
    }

    @Test
    fun picksDefaultPeer() {
        val peer = PublicPeerEntityFactory.build()
        runBlocking {
            publicPeerDao.save(peer)
        }

        testRule.start(SendPingActivity.getIntent(context))

        assertDisplayed(peer.publicAddress)
        assertEnabled(R.id.send)
    }
}
