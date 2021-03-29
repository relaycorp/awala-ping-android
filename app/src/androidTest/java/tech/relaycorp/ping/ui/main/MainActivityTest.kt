package tech.relaycorp.ping.ui.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.schibsted.spain.barista.assertion.BaristaContentDescriptionAssertions.assertContentDescription
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tech.relaycorp.ping.R
import tech.relaycorp.ping.data.database.dao.PingDao
import tech.relaycorp.ping.data.database.dao.PublicPeerDao
import tech.relaycorp.ping.test.AppTestProvider.component
import tech.relaycorp.ping.test.BaseActivityTestRule
import tech.relaycorp.ping.test.PingEntityFactory
import tech.relaycorp.ping.test.PublicPeerEntityFactory
import tech.relaycorp.ping.ui.common.DateTimeFormat
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    val testRule = BaseActivityTestRule(MainActivity::class, false)

    @Inject
    lateinit var publicPeerDao: PublicPeerDao

    @Inject
    lateinit var pingDao: PingDao

    @Before
    fun setUp() {
        component.inject(this)
    }

    @Test
    fun showsPings() = runBlockingTest {
        val peer = PublicPeerEntityFactory.build()
        publicPeerDao.save(peer)
        val ping = PingEntityFactory.build(peer)
        pingDao.save(ping)

        testRule.start()

        assertDisplayed(peer.publicAddress)
        assertDisplayed(DateTimeFormat.format(ping.sentAt))
        assertContentDescription(R.id.state, R.string.ping_sent)
    }
}
