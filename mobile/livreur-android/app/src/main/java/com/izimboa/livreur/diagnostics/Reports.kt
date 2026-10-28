package com.izimboa.livreur.diagnostics

import android.content.Context
import android.os.Build
import com.izimboa.livreur.BuildConfig
import com.izimboa.livreur.data.Api
import com.izimboa.livreur.tracking.LocationSource
import java.io.File
import kotlinx.serialization.Serializable

/**
 * Rapports techniques envoyés au serveur, pour corriger les problèmes du terrain sans câble ni journal :
 * - au démarrage : modèle du téléphone, version d'Android, services Google, état de la localisation ;
 * - après un plantage : la trace de l'erreur, gardée sur le téléphone et envoyée au lancement suivant.
 * Aucune donnée personnelle (ni nom, ni téléphone, ni position).
 */
object Reports {

    @Serializable
    data class Report(
        val kind: String,
        val app: String = "livreur-android",
        val version: String = BuildConfig.VERSION_NAME,
        val device: String = "${Build.MANUFACTURER} ${Build.MODEL}".take(100),
        val android: Int = Build.VERSION.SDK_INT,
        val googleServices: Boolean? = null,
        val providers: String? = null,
        val locationEnabled: Boolean? = null,
        val stack: String? = null,
    )

    private fun crashFile(context: Context) = File(context.filesDir, "last-crash.txt")

    /** À appeler au tout début : un plantage est écrit sur le téléphone avant que l'application ne se ferme. */
    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { crashFile(context).writeText(error.stackTraceToString().take(15_000)) }
            previous?.uncaughtException(thread, error)
        }
    }

    /** Erreur rattrapée (l'application continue) : envoyée comme un plantage, pour qu'on la corrige. */
    fun recordError(context: Context, error: Throwable) {
        runCatching { crashFile(context).writeText("(rattrapée) " + error.stackTraceToString().take(15_000)) }
    }

    /** Envoie le rapport de démarrage, et le plantage précédent s'il y en a un. */
    suspend fun sendAtStartup(context: Context, api: Api) {
        val location = LocationSource(context) {}
        runCatching {
            api.report(Report("START", googleServices = location.usesGoogle, providers = location.providers(),
                locationEnabled = location.enabled()))
        }
        val crash = crashFile(context)
        if (crash.exists()) {
            runCatching {
                api.report(Report("CRASH", googleServices = location.usesGoogle, stack = crash.readText()))
                crash.delete()
            }
        }
    }
}
