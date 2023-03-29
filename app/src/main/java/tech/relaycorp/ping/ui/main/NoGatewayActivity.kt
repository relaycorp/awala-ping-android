package tech.relaycorp.ping.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process.killProcess
import androidx.annotation.StringRes
import tech.relaycorp.ping.R
import tech.relaycorp.ping.databinding.ActivityNoGatewayBinding
import tech.relaycorp.ping.ui.BaseActivity

class NoGatewayActivity : BaseActivity() {

    private lateinit var binding: ActivityNoGatewayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoGatewayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation(R.drawable.ic_close)

        binding.download.setOnClickListener {
            openUrl(R.string.download_gateway)
        }
        binding.downloadOther.setOnClickListener {
            openUrl(R.string.download_gateway_other)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Force app closure
        killProcess(android.os.Process.myPid());
    }

    private fun openUrl(@StringRes urlRes: Int) {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(getString(urlRes)))
        )
    }

    companion object {
        fun getIntent(context: Context) = Intent(context, NoGatewayActivity::class.java)
    }
}
