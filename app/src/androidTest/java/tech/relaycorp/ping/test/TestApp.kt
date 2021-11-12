package tech.relaycorp.ping.test

import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.ping.App
import tech.relaycorp.ping.AppModule

open class TestApp : App() {
    override val component: AppTestComponent = DaggerAppTestComponent.builder()
        .appModule(AppModule(this))
        .build() as AppTestComponent

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
    }

    override suspend fun setupAwala() {
        Awala.setUp(this)
    }
}
