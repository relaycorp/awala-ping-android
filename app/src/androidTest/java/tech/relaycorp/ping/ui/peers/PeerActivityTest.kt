package tech.relaycorp.ping.ui.peers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import com.schibsted.spain.barista.rule.flaky.Repeat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import tech.relaycorp.ping.R
import tech.relaycorp.ping.data.database.dao.PublicPeerDao
import tech.relaycorp.ping.test.AppTestProvider.component
import tech.relaycorp.ping.test.AppTestProvider.context
import tech.relaycorp.ping.test.BaseActivityTestRule
import tech.relaycorp.ping.test.PublicPeerEntityFactory
import tech.relaycorp.ping.test.WaitAssertions.suspendWaitFor
import tech.relaycorp.ping.test.WaitAssertions.waitFor
import tech.relaycorp.ping.ui.peers.PeerActivity
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class PeerActivityTest {

    @Rule
    @JvmField
    val testRule = BaseActivityTestRule(PeerActivity::class, false)

    @Rule
    @JvmField
    val flakyChainRule = RuleChain.outerRule(FlakyTestRule())
        .around(testRule)

    @Inject
    lateinit var publicPeerDao: PublicPeerDao

    @Before
    fun setUp() {
        component.inject(this)
    }

    @Test
    fun displaysFields() {
        val peer = PublicPeerEntityFactory.build()
        runBlocking {
            publicPeerDao.save(peer)
        }

        testRule.start(PeerActivity.getIntent(context, peer.privateAddress))

        assertDisplayed(peer.publicAddress)
        assertDisplayed(peer.privateAddress)
    }

    @Test
    @AllowFlaky(attempts = 3)
    fun deletes() {
        val peer = PublicPeerEntityFactory.build()
        runBlocking {
            publicPeerDao.save(peer)
        }

        val activity = testRule.start(PeerActivity.getIntent(context, peer.privateAddress))

        clickOn(R.id.delete)
        clickOn(R.string.delete)

        waitFor {
            assertTrue(activity.isFinishing || activity.isDestroyed)
        }
        suspendWaitFor {
            assertTrue(publicPeerDao.get(peer.privateAddress).first()!!.deleted)
        }
    }
}
