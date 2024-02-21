package tech.relaycorp.ping

import android.app.Application
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.ping.common.Logging.logger
import tech.relaycorp.ping.common.di.AppComponent
import tech.relaycorp.ping.common.di.DaggerAppComponent
import tech.relaycorp.ping.domain.BootstrapData
import tech.relaycorp.ping.domain.ReceivePong
import tech.relaycorp.ping.ui.main.NoGatewayActivity
import java.util.logging.Level
import javax.inject.Inject

open class App : Application() {

    private val lifecycleScope by lazy { ProcessLifecycleOwner.get().lifecycleScope }

    open val component: AppComponent =
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()

    @Inject
    lateinit var bootstrapData: BootstrapData

    @Inject
    lateinit var receivePong: ReceivePong

    override fun onCreate() {
        super.onCreate()
        component.inject(this)

        lifecycleScope.launch {
            Awala.setUp(this@App)
            bindToGateway()
            GatewayClient.receiveMessages().collect(receivePong::receive)
        }
    }

    protected open fun bindToGateway() {
        GatewayClient.bindAutomatically(
            onBindSuccessful = {
                lifecycleScope.launch { bootstrapData.bootstrapIfNeeded() }
            },
            onBindFailure = {
                logger.log(Level.WARNING, "Gateway binding exception", it)
                openNoGateway()
            }
        )
    }

    private fun openNoGateway() {
        startActivity(
            NoGatewayActivity.getIntent(this)
                .addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
                )
        )
    }
}
