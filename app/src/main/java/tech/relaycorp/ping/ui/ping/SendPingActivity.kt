package tech.relaycorp.ping.ui.ping

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_send_ping.*
import kotlinx.android.synthetic.main.common_app_bar.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tech.relaycorp.ping.R
import tech.relaycorp.ping.common.di.ViewModelFactory
import tech.relaycorp.ping.domain.model.Peer
import tech.relaycorp.ping.ui.BaseActivity
import tech.relaycorp.ping.ui.common.SimpleItemSelectedListener
import tech.relaycorp.ping.ui.peers.PeersActivity
import javax.inject.Inject

class SendPingActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<SendPingViewModel>

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SendPingViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        setContentView(R.layout.activity_send_ping)
        setupNavigation()

        toolbar.inflateMenu(R.menu.send_ping)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.send -> viewModel.sendClicked()
            }
            true
        }

        toButton.setOnClickListener {
            openPeerPicker()
        }

        setupExpiresAt()

        viewModel
            .peer()
            .onEach { peerOp ->
                val hasPeer = peerOp.isPresent
                toButton.isVisible = !hasPeer
                toPeer.isVisible = hasPeer
                toPeer.text = if (hasPeer) {
                    peerOp.get().alias
                } else ""
            }
            .launchIn(lifecycleScope)

        viewModel
            .sendEnabled()
            .onEach {
                toolbar.menu.findItem(R.id.send).isEnabled = it
            }
            .launchIn(lifecycleScope)

        viewModel
            .errors()
            .onEach(this::showError)
            .launchIn(lifecycleScope)

        viewModel
            .finish()
            .onEach { finish() }
            .launchIn(lifecycleScope)

        results
            .onEach {
                if (it.requestCode == PICK_PEER && it.resultCode == Activity.RESULT_OK) {
                    it.data?.getParcelableExtra<Peer>(PeersActivity.EXTRA_PEER)?.let { peer ->
                        viewModel.peerPicked(peer)
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun setupExpiresAt() {
        val unitAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner_option,
            ExpireDuration.Unit.values().map { getString(it.stringRes) }
        )
        val valueAdapter = ArrayAdapter(
            this@SendPingActivity,
            R.layout.item_spinner_option,
            ExpireDuration.DEFAULT.unit.valueOptions.toMutableList()
        )

        expiresAtUnit.onItemSelectedListener = SimpleItemSelectedListener { position ->
            val unit = ExpireDuration.Unit.values()[position]
            valueAdapter.clear()
            valueAdapter.addAll(unit.valueOptions)
            viewModel.expiresAtChanged(
                ExpireDuration(
                    unit.valueOptions[expiresAtValue.selectedItemPosition],
                    unit
                )
            )
        }
        expiresAtValue.onItemSelectedListener = SimpleItemSelectedListener { position ->
            val unit = ExpireDuration.Unit.values()[expiresAtUnit.selectedItemPosition]
            viewModel.expiresAtChanged(
                ExpireDuration(
                    unit.valueOptions[position],
                    unit
                )
            )
        }

        expiresAtUnit.adapter = unitAdapter
        expiresAtValue.adapter = valueAdapter
        setExpiresAt(ExpireDuration.DEFAULT)
    }

    private fun setExpiresAt(expiresAt: ExpireDuration) {
        expiresAtUnit.setSelection(expiresAt.unit.ordinal)
        expiresAtValue.setSelection(expiresAt.unit.valueOptions.indexOf(expiresAt.value))
    }

    private fun openPeerPicker() {
        startActivityForResult(PeersActivity.getPickerIntent(this), PICK_PEER)
    }

    private fun showError(error: SendPingViewModel.Error) {
        messageManager.showError(
            when (error) {
                SendPingViewModel.Error.Sending -> R.string.ping_send_error
            }
        )
    }

    companion object {
        private const val PICK_PEER = 11

        fun getIntent(context: Context) = Intent(context, SendPingActivity::class.java)
    }
}
