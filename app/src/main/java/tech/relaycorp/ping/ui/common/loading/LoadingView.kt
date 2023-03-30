package tech.relaycorp.ping.ui.common.loading

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import tech.relaycorp.ping.databinding.ViewLoadingBinding

class LoadingView(context: Context) : FrameLayout(context) {

    private val binding = ViewLoadingBinding.inflate(LayoutInflater.from(context), this, true)

    fun setMessage(messageRes: Int) = binding.loadingMessage.setText(messageRes)
}
