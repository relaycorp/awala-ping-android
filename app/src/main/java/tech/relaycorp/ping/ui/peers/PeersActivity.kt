package tech.relaycorp.ping.ui.peers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.leinardi.android.speeddial.SpeedDialActionItem
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.activity_peers.*
import kotlinx.android.synthetic.main.common_app_bar.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tech.relaycorp.ping.R
import tech.relaycorp.ping.common.di.ViewModelFactory
import tech.relaycorp.ping.domain.model.Peer
import tech.relaycorp.ping.ui.BaseActivity
import tech.relaycorp.ping.ui.common.getColorCompat
import tech.relaycorp.ping.ui.common.getColorFromAttr
import javax.inject.Inject

class PeersActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<PeersViewModel>

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(PeersViewModel::class.java)
    }

    private val isPicker by lazy { intent.getBooleanExtra(EXTRA_PICKER, false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        setContentView(R.layout.activity_peers)
        setupNavigation(
            if (isPicker) R.drawable.ic_close else R.drawable.ic_back
        )

        toolbarTitle.text = getString(if (isPicker) R.string.peers_picker else R.string.peers)

        list.applyInsetter { type(navigationBars = true) { padding(bottom = true) } }
        addPeer.applyInsetter { type(navigationBars = true) { margin(bottom = true) } }
        list.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
        setupFABMenu()

        viewModel
            .peers()
            .onEach(this::updateList)
            .launchIn(lifecycleScope)
    }

    private fun setupFABMenu() {
        addPeer.addActionItem(
            SpeedDialActionItem.Builder(PUBLIC_PEER_ITEM_ID, R.drawable.ic_public)
                .setFabBackgroundColor(getColorFromAttr(R.attr.colorSurface))
                .setFabImageTintColor(getColorFromAttr(R.attr.colorOnSurface))
                .setLabelBackgroundColor(getColorCompat(android.R.color.transparent))
                .setLabel(R.string.peer_public)
                .create()
        )
        addPeer.addActionItem(
            SpeedDialActionItem.Builder(PRIVATE_PEER_ITEM_ID, R.drawable.ic_private)
                .setFabBackgroundColor(getColorFromAttr(R.attr.colorSurface))
                .setFabImageTintColor(getColorCompat(R.color.gray_light))
                .setLabelColor(getColorCompat(R.color.gray_light))
                .setLabelBackgroundColor(getColorCompat(android.R.color.transparent))
                .setLabel(R.string.peer_private)
                .create()
        )
        addPeer.setOnActionSelectedListener {
            when (it.id) {
                PUBLIC_PEER_ITEM_ID ->
                    startActivity(AddPublicPeerActivity.getIntent(this))
            }
            addPeer.close()
            true
        }
        addPeer.isVisible = !isPicker
    }

    private fun updateList(peers: List<Peer>) {
        list.withModels {
            peers.forEach { peer ->
                peerItemView {
                    id(peer.nodeId)
                    item(peer)
                    clickListener {
                        if (isPicker) {
                            finishWithResult(peer)
                        } else {
                            openPeer(peer)
                        }
                    }
                }
            }
        }
    }

    private fun openPeer(peer: Peer) {
        startActivity(PeerActivity.getIntent(this, peer.nodeId))
    }

    private fun finishWithResult(peer: Peer) {
        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_PEER, peer))
        finish()
    }

    companion object {
        private const val PUBLIC_PEER_ITEM_ID = 1
        private const val PRIVATE_PEER_ITEM_ID = 2

        private const val EXTRA_PICKER = "picker"
        const val EXTRA_PEER = "peer"

        fun getIntent(context: Context) = Intent(context, PeersActivity::class.java)

        fun getPickerIntent(context: Context) =
            getIntent(context).putExtra(EXTRA_PICKER, true)
    }
}
