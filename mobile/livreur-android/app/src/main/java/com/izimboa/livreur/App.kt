package com.izimboa.livreur

import android.app.Application
import kotlinx.coroutines.launch
import com.izimboa.livreur.auth.Auth
import com.izimboa.livreur.data.Api
import com.izimboa.livreur.offline.LocalStore
import com.izimboa.livreur.offline.Sync
import com.izimboa.livreur.tracking.Courier
import com.izimboa.livreur.tracking.Notifications

/** Point d'entrée : une seule instance de la connexion, de l'API, de la base locale et de l'état du livreur. */
class App : Application() {

    lateinit var auth: Auth private set
    lateinit var api: Api private set
    lateinit var store: LocalStore private set
    lateinit var courier: Courier private set
    lateinit var sync: Sync private set
    lateinit var file: com.izimboa.livreur.onboarding.CourierFile private set

    override fun onCreate() {
        super.onCreate()
        com.izimboa.livreur.diagnostics.Reports.install(this)
        auth = Auth(this)
        api = Api { auth.accessToken() }
        store = openStore()
        courier = Courier(this, api, store)
        file = com.izimboa.livreur.onboarding.CourierFile(api, auth)
        sync = Sync(store, api, onDelivery = courier::onServerDelivery, onRejected = courier::onRejected, scheduleRetry = { Sync.schedule(this) }, onPositionsSent = { courier.onPositionsSent(it) })
        courier.sync = sync
        Notifications.createChannels(this)
        com.izimboa.livreur.push.Push.init(this)
        if (sync.pending() > 0) Sync.schedule(this)
        kotlinx.coroutines.MainScope().launch(kotlinx.coroutines.Dispatchers.IO) {
            com.izimboa.livreur.diagnostics.Reports.sendAtStartup(this@App, api)
        }
    }

    /** Enregistre le jeton Firebase du téléphone (livreurs seulement), sans gêner si cela échoue. */
    suspend fun registerPush() {
        if (auth.session.value?.roles?.contains("livreur") != true) return
        val token = com.izimboa.livreur.push.Push.token() ?: return
        runCatching { api.registerPushToken(token) }
    }

    /** Déconnexion : plus rien du livreur ne reste sur le téléphone. */
    fun signOut() = auth.logoutIntent().also {
        stopService(android.content.Intent(this, com.izimboa.livreur.tracking.TrackingService::class.java))
        com.izimboa.livreur.push.Push.forget()
        store.clear()
        courier.closeFinishedDelivery()
        file.reset()
    }

    /** Clé perdue (restauration, réinitialisation du Keystore) : la base illisible est recréée vide. */
    private fun openStore(): LocalStore = try {
        LocalStore(this).also { it.writableDatabase }
    } catch (e: Exception) {
        deleteDatabase("izimboa-livreur.db")
        LocalStore(this).also { it.writableDatabase }
    }
}
