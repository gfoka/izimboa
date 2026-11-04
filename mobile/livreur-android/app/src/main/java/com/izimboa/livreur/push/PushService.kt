package com.izimboa.livreur.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.izimboa.livreur.App
import com.izimboa.livreur.tracking.Notifications
import kotlinx.coroutines.runBlocking

/** Messages Firebase : nouveau jeton du téléphone, nouvelle course proposée. */
class PushService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val app = application as App
        if (app.auth.session.value?.roles?.contains("livreur") != true) return
        runBlocking { runCatching { app.api.registerPushToken(token) } }
    }

    /** Appelé hors du fil principal ; quelques secondes sont permises pour relire l'offre et faire sonner. */
    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data["type"] != "offer") return
        val app = application as App
        runBlocking {
            runCatching { app.courier.waitForOffer(seconds = 0) }
                .onSuccess { offer -> offer?.let { Notifications.offer(this@PushService, it) } }
                .onFailure { Log.w("Push", "Offre non relue", it) }
        }
    }
}
