package tech.relaycorp.ping.ui.peers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.adevinta.android.barista.rule.flaky.AllowFlaky
import com.adevinta.android.barista.rule.flaky.FlakyTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import tech.relaycorp.ping.R
import tech.relaycorp.ping.test.AppTestProvider.component
import tech.relaycorp.ping.test.BaseActivityTestRule

@RunWith(AndroidJUnit4::class)
class AddPublicPeerActivityTest {

    @Rule
    @JvmField
    val testRule = BaseActivityTestRule(PeersActivity::class, false)

    @Rule
    @JvmField
    val flakyChainRule = RuleChain.outerRule(FlakyTestRule())
        .around(testRule)

    @Before
    fun setUp() {
        component.inject(this)
    }

    @Test
    @AllowFlaky(attempts = 3)
    fun addPublicPeerMissingCertificate() {
        testRule.start()
        clickOn(R.id.addPeer)
        clickOn(R.string.peer_public)

        clickOn(R.id.save)
        assertDisplayed(R.string.peer_add_missing_conn_params_file)
    }
}
