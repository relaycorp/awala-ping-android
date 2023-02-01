package tech.relaycorp.ping.ui.main

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Process.killProcess
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_no_gateway.actionBtn
import kotlinx.android.synthetic.main.activity_no_gateway.messageTv
import kotlinx.android.synthetic.main.activity_no_gateway.secondaryActionBtn
import kotlinx.android.synthetic.main.activity_no_gateway.titleTv
import tech.relaycorp.ping.R
import tech.relaycorp.ping.ui.BaseActivity


class NoGatewayActivity : BaseActivity() {

    private val downloadPlaystore
        get() = String.format(
            getString(R.string.download_playstore),
            getString(R.string.awala_package)
        )

    private val downloadOther
        get() = getString(R.string.download_gateway_other)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_gateway)
        setupNavigation(R.drawable.ic_close)

        val isGatewayInstalled = intent.getBooleanExtra(GATEWAY_INSTALLED_KEY, false)

        actionBtn.setOnClickListener {
            if (isGatewayInstalled) {
                openGateway()
            } else {
                openUrl(downloadPlaystore)
            }
        }

        secondaryActionBtn.setOnClickListener {
            openUrl(downloadOther)
        }

        titleTv.text = getString(
            if (isGatewayInstalled) R.string.bad_gateway_title
            else R.string.no_gateway_title
        )
        messageTv.text = getString(
            if (isGatewayInstalled) R.string.bad_gateway_message
            else R.string.no_gateway_message
        )
        actionBtn.text = getString(
            if (isGatewayInstalled) R.string.bad_gateway_download
            else R.string.no_gateway_download
        )
        secondaryActionBtn.isVisible = !isGatewayInstalled
    }


    override fun onDestroy() {
        super.onDestroy()
        // Force app closure
        killProcess(android.os.Process.myPid());
    }

    private fun openUrl(urlStr: String) {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(urlStr))
        )
    }

    private fun openGateway() {
        val intent = packageManager.getLaunchIntentForPackage(getString(R.string.awala_package))

        if (intent != null &&
            packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY
            ).isNotEmpty()
        ) {
            startActivity(intent)
        } else {
            openUrl(downloadPlaystore)
        }
    }


    companion object {
        @VisibleForTesting
        const val GATEWAY_INSTALLED_KEY = "gatewayInstalled"

        fun getIntent(context: Context, isGatewayInstalled: Boolean) =
            Intent(context, NoGatewayActivity::class.java).apply {
                putExtra(GATEWAY_INSTALLED_KEY, isGatewayInstalled)
            }
    }
}
