package tech.relaycorp.ping.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import com.google.android.material.appbar.AppBarLayout
import dev.chrisbanes.insetter.applyInsetter
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

    private val appBar: AppBarLayout? get() = findViewById(R.id.appBar)
    protected val toolbar: Toolbar? get() = findViewById(R.id.toolbar)
    protected val toolbarTitle: TextView? get() = findViewById(R.id.toolbarTitle)

    protected val results get() = _results.asFlow()
    private val _results = PublishFlow<ActivityResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup edge-to-edge UI
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        _results.trySendBlocking(ActivityResult(requestCode, resultCode, data))
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
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
