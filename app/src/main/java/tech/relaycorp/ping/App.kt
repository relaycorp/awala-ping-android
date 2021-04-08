package tech.relaycorp.ping

import android.app.Application
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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

    val coroutineContext = Dispatchers.Default + SupervisorJob()

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

        CoroutineScope(coroutineContext).launch {
            setupAwala()
        }
    }

    protected open suspend fun setupAwala() {
        Awala.setup(this)
        try {
            GatewayClient.bind()
            bootstrapData.bootstrapIfNeeded()
            GatewayClient.receiveMessages().collect(receivePong::receive)
        } catch (exp: GatewayBindingException) {
            logger.log(Level.WARNING, "Gateway binding exception", exp)
            openNoGateway()
        }
    }

    private fun openNoGateway() {
        startActivity(
            NoGatewayActivity.getIntent(this)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }
}
