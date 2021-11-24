package tech.relaycorp.ping.ui.peers

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
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
import tech.relaycorp.ping.test.ActivityAssertions.waitForCurrentActivityToBe
import tech.relaycorp.ping.test.AppTestProvider.component
import tech.relaycorp.ping.test.AppTestProvider.context
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
    fun addPublicPeerSuccessfully() {
        testRule.start()
        clickOn(R.id.addPeer)
        clickOn(R.string.peer_public)

        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT))
            .respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    Intent().setData(
                        Uri.parse("android.resource://${context.packageName}/${R.raw.default_public_peer_connection_params}")
                    )
                )
            )
        clickOn(R.string.peer_conn_params_file_button)

        clickOn(R.id.save)

        waitForCurrentActivityToBe(PeersActivity::class)
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
