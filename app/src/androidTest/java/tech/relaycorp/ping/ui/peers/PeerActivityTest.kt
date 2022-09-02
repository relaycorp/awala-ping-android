package tech.relaycorp.ping.ui.peers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.adevinta.android.barista.rule.flaky.AllowFlaky
import com.adevinta.android.barista.rule.flaky.FlakyTestRule
import com.adevinta.android.barista.rule.flaky.Repeat
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

        testRule.start(PeerActivity.getIntent(context, peer.nodeId))

        assertDisplayed(peer.internetAddress)
        assertDisplayed(peer.nodeId)
    }

    @Test
    @AllowFlaky(attempts = 3)
    fun deletes() {
        val peer = PublicPeerEntityFactory.build()
        runBlocking {
            publicPeerDao.save(peer)
        }

        val activity = testRule.start(PeerActivity.getIntent(context, peer.nodeId))

        clickOn(R.id.delete)
        clickOn(R.string.delete)

        waitFor {
            assertTrue(activity.isFinishing || activity.isDestroyed)
        }
        suspendWaitFor {
            assertTrue(publicPeerDao.get(peer.nodeId).first()!!.deleted)
        }
    }
}
