package com.izimboa.livreur.push

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.izimboa.livreur.BuildConfig
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Firebase Cloud Messaging (décision 76) : le serveur réveille l'application pour une nouvelle course, même si
 * Android l'a endormie ou arrêtée. Initialisé à la main (pas de fichier google-services.json ni de plugin Gradle).
 */
object Push {

    private const val TAG = "Push"

    fun init(context: Context) {
        if (FirebaseApp.getApps(context).isNotEmpty()) return
        runCatching {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                    .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .build(),
            )
        }.onFailure { Log.w(TAG, "Firebase non initialisé", it) }
    }

    /** Jeton de ce téléphone, ou null (pas de services Google, pas de réseau). */
    suspend fun token(): String? = runCatching {
        suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { Log.w(TAG, "Jeton indisponible", it); cont.resume(null) }
        }
    }.getOrNull()

    /** Déconnexion : le jeton est détruit, le serveur l'oubliera au prochain envoi refusé. */
    fun forget() {
        runCatching { FirebaseMessaging.getInstance().deleteToken() }
    }
}
