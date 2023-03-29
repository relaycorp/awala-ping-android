package tech.relaycorp.ping.ui.peers

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.relaycorp.ping.databinding.ItemPeerBinding
import tech.relaycorp.ping.domain.model.Peer
import tech.relaycorp.ping.ui.BaseView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PeerItemView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseView(context, attrs, defStyleAttr) {

    private val binding = ItemPeerBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setItem(item: Peer) {
        binding.alias.text = item.alias
    }

    @CallbackProp
    fun setClickListener(listener: (() -> Unit)?) {
        setOnClickListener { listener?.invoke() }
    }
}
