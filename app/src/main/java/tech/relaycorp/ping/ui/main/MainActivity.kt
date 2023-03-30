package tech.relaycorp.ping.ui.main

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tech.relaycorp.ping.R
import tech.relaycorp.ping.common.di.ViewModelFactory
import tech.relaycorp.ping.databinding.ActivityMainBinding
import tech.relaycorp.ping.domain.model.Ping
import tech.relaycorp.ping.ui.BaseActivity
import tech.relaycorp.ping.ui.common.dummyItemView
import tech.relaycorp.ping.ui.peers.PeersActivity
import tech.relaycorp.ping.ui.ping.SendPingActivity
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<MainViewModel>

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.list.applyInsetter { type(navigationBars = true) { padding(bottom = true) } }
        binding.sendPing.applyInsetter { type(navigationBars = true) { margin(bottom = true) } }
        binding.list.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))

        toolbar?.inflateMenu(R.menu.main)
        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.peers -> openPeers()
                R.id.about -> openAbout()
            }
            true
        }

        binding.sendPing.setOnClickListener {
            startActivity(SendPingActivity.getIntent(this))
        }

        viewModel
            .pings()
            .onEach(this::updateList)
            .launchIn(lifecycleScope)
    }

    private fun openPeers() {
        startActivity(PeersActivity.getIntent(this))
    }

    private fun openAbout() {
        startActivity(AboutActivity.getIntent(this))
    }

    private fun updateList(pings: List<Ping>) {
        binding.list.withModels {
            dummyItemView {
                id("top")
            }

            pings.forEach { ping ->
                pingItemView {
                    id(ping.pingId)
                    item(ping)
                }
            }
        }
    }
}
