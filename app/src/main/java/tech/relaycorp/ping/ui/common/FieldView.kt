package tech.relaycorp.ping.ui.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.res.use
import androidx.core.view.isVisible
import tech.relaycorp.ping.R
import tech.relaycorp.ping.databinding.ViewFieldBinding
import tech.relaycorp.ping.ui.BaseView

class FieldView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseView(context, attrs, defStyleAttr) {

    private val binding =
        ViewFieldBinding.inflate(LayoutInflater.from(context), this, true)

    var label: String?
        get() = binding.labelText.text.toString()
        set(value) {
            binding.labelText.text = value
        }

    var value: String
        get() = binding.valueText.text.toString()
        set(value) {
            binding.valueText.text = value
        }

    private var copyEnabled: Boolean
        get() = binding.copy.isVisible
        set(value) {
            binding.copy.isVisible = value
        }

    private val clipboard by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.Field, 0, 0).use {
            label = it.getString(R.styleable.Field_label) ?: ""
            copyEnabled = it.getBoolean(R.styleable.Field_copyEnabled, false)
        }

        binding.copy.setOnClickListener {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
            messageManager.showMessage(R.string.copy_confirm)
        }
    }
}
