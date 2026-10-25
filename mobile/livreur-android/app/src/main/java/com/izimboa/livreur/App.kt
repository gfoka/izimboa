package com.izimboa.livreur

import android.app.Application
import com.izimboa.livreur.auth.Auth
import com.izimboa.livreur.data.Api
import com.izimboa.livreur.tracking.Courier
import com.izimboa.livreur.tracking.Notifications

/** Point d'entrée : une seule instance de la connexion, de l'API et de l'état du livreur. */
class App : Application() {

    lateinit var auth: Auth private set
    lateinit var api: Api private set
    lateinit var courier: Courier private set

    override fun onCreate() {
        super.onCreate()
        auth = Auth(this)
        api = Api { auth.accessToken() }
        courier = Courier(this, api)
        Notifications.createChannels(this)
    }
}
