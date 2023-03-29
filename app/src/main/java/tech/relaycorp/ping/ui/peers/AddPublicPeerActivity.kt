package tech.relaycorp.ping.ui.peers

import android.app.Activity
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
import tech.relaycorp.ping.databinding.ActivityAddPublicPeerBinding
import tech.relaycorp.ping.ui.BaseActivity
import javax.inject.Inject

class AddPublicPeerActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<AddPublicPeerViewModel>

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(AddPublicPeerViewModel::class.java)
    }

    private lateinit var binding: ActivityAddPublicPeerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        binding = ActivityAddPublicPeerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation(R.drawable.ic_close)

        toolbar?.inflateMenu(R.menu.add_peer)
        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save -> viewModel.saveClicked()
            }
            true
        }

        binding.connectionParamsButton.setOnClickListener {
            openFileDialog()
        }
        binding.connectionParamsClear.setOnClickListener {
            viewModel.removeConnectionParamsFileClicked()
        }

        viewModel
            .connectionParamsFile()
            .onEach {
                val hasConnectionParams = it.isPresent
                binding.connectionParamsButton.isVisible = !hasConnectionParams
                binding.connectionParamsName.isVisible = hasConnectionParams
                binding.connectionParamsName.text = if (hasConnectionParams) {
                    it.get().ifBlank { getString(R.string.peer_conn_params_file_picked) }
                } else ""
                binding.connectionParamsClear.isVisible = hasConnectionParams
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
                if (it.requestCode == PICK_CONNECTION_PARAMS && it.resultCode == Activity.RESULT_OK) {
                    it.data?.data?.let { uri -> viewModel.connectionParamsFilePicked(uri) }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun openFileDialog() {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
            },
            PICK_CONNECTION_PARAMS
        )
    }

    private fun showError(error: AddPublicPeerViewModel.Error) {
        messageManager.showError(
            when (error) {
                AddPublicPeerViewModel.Error.MissingConnectionParams ->
                    R.string.peer_add_missing_conn_params_file
                AddPublicPeerViewModel.Error.InvalidConnectionParams ->
                    R.string.peer_add_invalid_conn_params_file
                AddPublicPeerViewModel.Error.GenericSave ->
                    R.string.peer_add_error
            }
        )
    }

    companion object {
        private const val PICK_CONNECTION_PARAMS = 11

        fun getIntent(context: Context) = Intent(context, AddPublicPeerActivity::class.java)
    }
}
