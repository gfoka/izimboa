package com.izimboa.livreur.tracking

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Looper
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.izimboa.livreur.App
import com.izimboa.livreur.data.PointBody
import java.util.concurrent.ConcurrentLinkedDeque
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Service de premier plan (notification permanente) tant que le livreur est en ligne :
 * - position toutes les ~5 s, envoyée par paquets (gardée en mémoire si le réseau coupe) ;
 * - écoute des courses proposées toutes les 4 s, sonnerie à l'arrivée d'une nouvelle.
 * Démarré depuis l'écran (application au premier plan) : pas besoin de la permission « localisation en arrière-plan ».
 */
class TrackingService : LifecycleService() {

    private val app get() = application as App
    private val pending = ConcurrentLinkedDeque<PointBody>()
    private val location by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (l in result.locations) {
                app.courier.onLocation(l)
                pending.addLast(Courier.point(l))
                while (pending.size > MAX_BUFFER) pending.pollFirst()
            }
        }
    }
    private var started = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        ServiceCompat.startForeground(this, Notifications.ID_ONLINE, Notifications.online(this), type)
        if (!started) {
            started = true
            startLocation()
            lifecycleScope.launch { sendPositionsLoop() }
            lifecycleScope.launch { offersLoop() }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission") // vérifiée par l'écran avant de passer en ligne
    private fun startLocation() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000)
            .setMinUpdateIntervalMillis(3_000)
            .setWaitForAccurateLocation(false)
            .build()
        runCatching { location.requestLocationUpdates(request, callback, Looper.getMainLooper()) }
    }

    private suspend fun sendPositionsLoop() {
        while (lifecycleScope.isActive) {
            delay(10_000)
            if (pending.isEmpty()) continue
            val batch = generateSequence { pending.pollFirst() }.take(200).toList()
            try {
                app.api.positions(batch)
            } catch (e: Exception) {
                batch.asReversed().forEach { pending.addFirst(it) } // réseau coupé : on renverra plus tard
            }
        }
    }

    private suspend fun offersLoop() {
        var tick = 0
        while (lifecycleScope.isActive) {
            runCatching {
                app.courier.pollOffer()?.let { Notifications.offer(this, it) }
                if (tick++ % 4 == 0) app.courier.refreshDelivery() // attribution manuelle par un opérateur
            }
            delay(4_000)
        }
    }

    override fun onDestroy() {
        location.removeLocationUpdates(callback)
        super.onDestroy()
    }

    private companion object { const val MAX_BUFFER = 3_000 }
}
