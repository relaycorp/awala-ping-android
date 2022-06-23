package tech.relaycorp.ping.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.android.synthetic.main.common_app_bar.*
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.asFlow
import tech.relaycorp.ping.ui.common.MessageManager
import tech.relaycorp.ping.App
import tech.relaycorp.ping.R
import tech.relaycorp.ping.common.PublishFlow
import tech.relaycorp.ping.ui.common.ActivityResult
import tech.relaycorp.ping.ui.common.loading.LoadingManager

abstract class BaseActivity : AppCompatActivity() {

    private val app get() = applicationContext as App
    val component by lazy { app.component.activityComponent() }

    protected val messageManager by lazy { MessageManager(this) }
    protected val loadingManager by lazy { LoadingManager(this) }

    protected val results get() = _results.asFlow()
    private val _results = PublishFlow<ActivityResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup edge-to-edge UI
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        _results.trySendBlocking(ActivityResult(requestCode, resultCode, data))
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        toolbarTitle?.text = title
        appBar?.applyInsetter {
            type(statusBars = true) {
                padding(top = true)
            }
        }
    }

    protected fun setupNavigation(
        @DrawableRes icon: Int = R.drawable.ic_back,
        clickListener: (() -> Unit) = { finish() }
    ) {
        toolbar?.setNavigationIcon(icon)
        toolbar?.setNavigationOnClickListener { clickListener.invoke() }
    }
}
