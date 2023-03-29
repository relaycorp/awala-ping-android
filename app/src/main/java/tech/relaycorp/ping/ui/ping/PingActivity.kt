package tech.relaycorp.ping.ui.ping

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tech.relaycorp.ping.R
import tech.relaycorp.ping.common.di.ViewModelFactory
import tech.relaycorp.ping.databinding.ActivityPingBinding
import tech.relaycorp.ping.domain.model.Ping
import tech.relaycorp.ping.ui.BaseActivity
import tech.relaycorp.ping.ui.common.DateTimeFormat
import javax.inject.Inject

class PingActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<PingViewModel>

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(PingViewModel::class.java)
    }

    private lateinit var binding: ActivityPingBinding

    private val pingId by lazy { intent.getStringExtra(PING_ID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        binding = ActivityPingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()

        viewModel
            .ping()
            .onEach {
                binding.recipient.text = it.peer.alias
                binding.state.setImageResource(
                    when (it.state) {
                        Ping.State.Sent -> R.drawable.ic_check
                        Ping.State.SendAndReplied -> R.drawable.ic_double_check
                        Ping.State.Expired -> R.drawable.ic_expired
                    }
                )
                binding.state.contentDescription = getString(
                    when (it.state) {
                        Ping.State.Sent -> R.string.ping_state_sent
                        Ping.State.SendAndReplied -> R.string.ping_state_replied
                        Ping.State.Expired -> R.string.ping_state_expired
                    }
                )
                binding.sentAtField.value = DateTimeFormat.format(it.sentAt)
                binding.pingIdField.value = it.pingId
                binding.parcelIdField.value = it.parcelId
                binding.expiresAtField.value = DateTimeFormat.format(it.expiresAt)
                binding.pongReceivedField.isVisible = it.pongReceivedAt != null
                binding.pongReceivedField.value = DateTimeFormat.format(it.pongReceivedAt)
            }
            .launchIn(lifecycleScope)

        pingId?.let { viewModel.pingIdReceived(it) } ?: finish()
    }

    companion object {
        private const val PING_ID = "pingId"

        fun getIntent(context: Context, pingId: String) =
            Intent(context, PingActivity::class.java)
                .putExtra(PING_ID, pingId)
    }
}
