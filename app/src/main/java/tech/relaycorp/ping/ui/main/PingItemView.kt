package tech.relaycorp.ping.ui.main

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import tech.relaycorp.ping.R
import tech.relaycorp.ping.common.flowInterval
import tech.relaycorp.ping.databinding.ItemPingBinding
import tech.relaycorp.ping.domain.model.Ping
import tech.relaycorp.ping.ui.BaseView
import tech.relaycorp.ping.ui.common.DateTimeFormat
import tech.relaycorp.ping.ui.ping.PingActivity
import kotlin.time.Duration.Companion.seconds

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PingItemView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseView(context, attrs, defStyleAttr) {

    private val binding = ItemPingBinding.inflate(LayoutInflater.from(context), this, true)

    private var refreshScope: CoroutineScope? = null

    @ModelProp
    fun setItem(item: Ping) {
        binding.recipient.text = item.peer.alias

        refresh { setRefreshableState(item) }

        setOnClickListener {
            context.startActivity(
                PingActivity.getIntent(context, pingId = item.pingId)
            )
        }
    }

    private fun setRefreshableState(item: Ping) {
        binding.sentAt.text = DateTimeFormat.format(item.sentAt)

        binding.state.setImageResource(
            when (item.state) {
                Ping.State.Sent -> R.drawable.ic_check
                Ping.State.SendAndReplied -> R.drawable.ic_double_check
                Ping.State.Expired -> R.drawable.ic_expired
            }
        )
        binding.state.contentDescription = resources.getString(
            when (item.state) {
                Ping.State.Sent -> R.string.ping_sent
                Ping.State.SendAndReplied -> R.string.ping_sent_and_replied
                Ping.State.Expired -> R.string.ping_expired
            }
        )
    }

    private fun refresh(action: () -> Unit) {
        refreshScope?.cancel()
        refreshScope = (activity.lifecycleScope.plus(SupervisorJob())).also { refreshScope ->
            flowInterval(10.seconds)
                .onEach { action() }
                .launchIn(refreshScope)
        }
    }
}
