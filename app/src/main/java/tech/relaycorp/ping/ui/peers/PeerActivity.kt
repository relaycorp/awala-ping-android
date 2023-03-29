package tech.relaycorp.ping.ui.peers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tech.relaycorp.ping.R
import tech.relaycorp.ping.common.di.ViewModelFactory
import tech.relaycorp.ping.databinding.ActivityPeerBinding
import tech.relaycorp.ping.ui.BaseActivity
import javax.inject.Inject

class PeerActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<PeerViewModel>

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(PeerViewModel::class.java)
    }

    private lateinit var binding: ActivityPeerBinding

    private val privateAddress by lazy { intent.getStringExtra(PEER_PRIVATE_ADDRESS) }

    private var deleteConfirmDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        binding = ActivityPeerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()

        toolbar?.inflateMenu(R.menu.peer)
        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> showDeleteConfirmDialog()
            }
            true
        }

        viewModel
            .peer()
            .onEach {
                binding.alias.text = it.alias
                binding.idField.value = it.nodeId
            }
            .launchIn(lifecycleScope)

        viewModel
            .finish()
            .onEach { finish() }
            .launchIn(lifecycleScope)

        privateAddress?.let { viewModel.privateAddressReceived(it) } ?: finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (deleteConfirmDialog?.isShowing == true) deleteConfirmDialog?.dismiss()
        deleteConfirmDialog = null
    }

    private fun showDeleteConfirmDialog() {
        deleteConfirmDialog = MaterialAlertDialogBuilder(this)
            .setMessage(R.string.peer_delete_confirm)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteClicked() }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    companion object {
        private const val PEER_PRIVATE_ADDRESS = "peerPrivateAddress"

        fun getIntent(context: Context, peerPrivateAddress: String) =
            Intent(context, PeerActivity::class.java)
                .putExtra(PEER_PRIVATE_ADDRESS, peerPrivateAddress)
    }
}
