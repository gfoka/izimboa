package com.izimboa.livreur.tracking

import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.izimboa.livreur.App
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Service de premier plan (notification permanente) tant que le livreur est en ligne. Économe en batterie :
 *
 * - GPS adapté à la situation (décision n° 63) :
 *   en attente de course → précision « quartier », un point par 50 m parcourus, au plus toutes les 30 s ;
 *   en course → GPS précis, un point par 15 m parcourus : beaucoup de points quand la moto roule vite,
 *   aucun quand elle est arrêtée (la fréquence suit la vitesse). Les points sont livrés par paquets
 *   par Android, sans réveiller l'application à chaque fois.
 * - Envois regroupés : toutes les 20 s en course (suivi en direct), toutes les 60 s en attente.
 * - Courses : une requête qui attend jusqu'à 20 s côté serveur (2 à 3 réveils radio par minute).
 * - Tout est d'abord écrit dans la base chiffrée : données coupées ou téléphone éteint, rien n'est perdu.
 */
class TrackingService : LifecycleService() {

    private val app get() = application as App
    private val location by lazy { LocationSource(this) { app.courier.recordLocation(it) } }
    private val network = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(n: Network) {
            lifecycleScope.launch { runCatching { app.sync.flush() } } // le réseau revient : on vide la file tout de suite
        }
    }
    private var started = false

    private val delivering get() = app.courier.state.value.delivery != null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        try {
            ServiceCompat.startForeground(this, Notifications.ID_ONLINE, Notifications.online(this), type)
        } catch (e: Exception) {
            // Android refuse (relance en arrière-plan, permission retirée…) : on s'arrête proprement au lieu de planter.
            // Le service repart quand le livreur rouvre l'application.
            app.courier.onServiceRefused()
            stopSelf()
            return START_NOT_STICKY
        }
        if (!started) {
            started = true
            runCatching { getSystemService(ConnectivityManager::class.java).registerDefaultNetworkCallback(network) }
            lifecycleScope.launch {
                app.courier.state.map { it.delivery != null }.distinctUntilChanged().collect { location.start(it) }
            }
            app.courier.onLocationSettings(location.enabled())
            location.firstFix()
            lifecycleScope.launch { uploadLoop() }
            lifecycleScope.launch { offersLoop() }
        }
        return START_STICKY
    }

    private suspend fun uploadLoop() {
        delay(5_000)
        while (lifecycleScope.isActive) {
            app.courier.onLocationSettings(location.enabled())
            if (!delivering) app.courier.heartbeat(everyMs = 55_000) // immobile : reste visible sans rallumer le GPS
            runCatching { app.sync.flush() }
            delay(if (delivering) 20_000 else 45_000)
        }
    }

    private suspend fun offersLoop() {
        while (lifecycleScope.isActive) {
            try {
                if (delivering) {
                    delay(30_000)
                    app.courier.refreshDelivery() // attribution ou annulation par un opérateur
                } else {
                    app.courier.waitForOffer(seconds = 20)?.let { Notifications.offer(this, it) }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                delay(10_000) // pas de réseau : on réessaie plus tard, sans s'acharner
            }
        }
    }

    override fun onDestroy() {
        location.stop()
        runCatching { getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(network) }
        super.onDestroy()
    }
}
