package tech.relaycorp.ping.test

import android.app.Activity
import android.content.Intent
import androidx.test.rule.ActivityTestRule
import com.adevinta.android.barista.rule.cleardata.ClearFilesRule
import com.adevinta.android.barista.rule.cleardata.ClearPreferencesRule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.reflect.KClass

class BaseActivityTestRule<T : Activity>(
    activityClass: KClass<T>,
    launchActivity: Boolean = true
) : TestRule {

    private val clearPreferencesRule: ClearPreferencesRule = ClearPreferencesRule()
    private val clearDatabaseRule: ClearTestDatabaseRule = ClearTestDatabaseRule()
    private val clearFilesRule: ClearFilesRule = ClearFilesRule()
    private val intentsRule: IntentsRule = IntentsRule()
    private val activityTestRule: ActivityTestRule<T> = ActivityTestRule(
        activityClass.java,
        true,
        launchActivity
    )

    override fun apply(base: Statement, description: Description): Statement {
        return RuleChain.outerRule(activityTestRule)
            .around(clearPreferencesRule)
            .around(clearDatabaseRule)
            .around(clearFilesRule)
            .around(intentsRule)
            .apply(base, description)
    }

    fun start(intent: Intent? = null): T = activityTestRule.launchActivity(intent)
}
