package tech.relaycorp.ping.ui.ping

import android.content.ClipboardManager
import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adevinta.android.barista.assertion.BaristaContentDescriptionAssertions.assertContentDescription
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.rule.flaky.AllowFlaky
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tech.relaycorp.ping.R
import tech.relaycorp.ping.data.database.dao.PingDao
import tech.relaycorp.ping.data.database.dao.PublicPeerDao
import tech.relaycorp.ping.test.AppTestProvider.component
import tech.relaycorp.ping.test.AppTestProvider.context
import tech.relaycorp.ping.test.BaseActivityTestRule
import tech.relaycorp.ping.test.PingEntityFactory
import tech.relaycorp.ping.test.PublicPeerEntityFactory
import tech.relaycorp.ping.ui.common.DateTimeFormat
import java.time.ZonedDateTime
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class PingActivityTest {

    @Rule
    @JvmField
    val testRule = BaseActivityTestRule(PingActivity::class, false)

    @Inject
    lateinit var publicPeerDao: PublicPeerDao

    @Inject
    lateinit var pingDao: PingDao

    @Before
    fun setUp() {
        component.inject(this)
    }

    @Test
    fun displaysPing() {
        val peer = PublicPeerEntityFactory.build()
        val ping = PingEntityFactory.build(peer)
        runBlocking {
            publicPeerDao.save(peer)
            pingDao.save(ping)
        }

        testRule.start(PingActivity.getIntent(context, ping.pingId))

        assertDisplayed(peer.internetAddress)
        assertDisplayed(ping.pingId)
        assertContentDescription(R.id.state, R.string.ping_state_sent)
        assertDisplayed(DateTimeFormat.format(ping.sentAt))
        assertDisplayed(DateTimeFormat.format(ping.expiresAt))
        assertNotDisplayed(R.string.ping_pong_received_at)
    }

    @Test
    @AllowFlaky(attempts = 3)
    fun displaysPingWithPong() {
        val peer = PublicPeerEntityFactory.build()
        val ping = PingEntityFactory.build(peer).copy(
            pongReceivedAt = ZonedDateTime.now()
        )
        runBlocking {
            publicPeerDao.save(peer)
            pingDao.save(ping)
        }

        testRule.start(PingActivity.getIntent(context, ping.pingId))

        assertDisplayed(peer.internetAddress)
        assertDisplayed(ping.pingId)
        assertContentDescription(R.id.state, R.string.ping_state_replied)
        assertDisplayed(DateTimeFormat.format(ping.sentAt))
        assertDisplayed(DateTimeFormat.format(ping.expiresAt))
        assertDisplayed(DateTimeFormat.format(ping.pongReceivedAt))
    }

    @Test
    fun copyPingId() {
        val peer = PublicPeerEntityFactory.build()
        val ping = PingEntityFactory.build(peer)
        runBlocking {
            publicPeerDao.save(peer)
            pingDao.save(ping)
        }

        testRule.start(PingActivity.getIntent(context, ping.pingId))


        onView(
            allOf(
                withId(R.id.copy),
                isDescendantOfA(withId(R.id.pingIdField))
            )
        ).perform(click())

        assertDisplayed(R.string.copy_confirm)

        runBlocking(Dispatchers.Main) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            assertEquals(
                ping.pingId,
                clipboard.primaryClip?.getItemAt(0)?.text.toString()
            )
        }
    }
}
