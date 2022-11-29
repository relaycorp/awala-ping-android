package tech.relaycorp.ping.ui.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tech.relaycorp.ping.test.AppTestProvider.component
import tech.relaycorp.ping.test.BaseActivityTestRule
import tech.relaycorp.ping.R

@RunWith(AndroidJUnit4::class)
class NoGatewayActivityTest {

    @Rule
    @JvmField
    val testRule = BaseActivityTestRule(NoGatewayActivity::class, false)

    @Before
    fun setUp() {
        component.inject(this)
    }

    @Test
    fun defaultGatewayNotInstalledTest() {
        testRule.start()

        assertDisplayed(R.string.no_gateway_title)
        assertDisplayed(R.string.no_gateway_message)
        assertDisplayed(R.string.no_gateway_download)
        assertDisplayed(R.string.no_gateway_download_other)

    }

    @Test
    fun gatewayInstalledTest() {
        testRule.start(
            NoGatewayActivity.getIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                true
            )
        )

        assertDisplayed(R.string.bad_gateway_title)
        assertDisplayed(R.string.bad_gateway_message)
        assertDisplayed(R.string.bad_gateway_download)
        assertNotDisplayed(R.id.secondaryActionBtn)
    }


}

