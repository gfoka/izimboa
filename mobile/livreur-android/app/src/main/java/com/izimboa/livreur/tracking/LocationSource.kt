package com.izimboa.livreur.tracking

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Source de positions. Avec les services Google (la plupart des téléphones) : « Fused Location », le plus économe.
 * Sans eux (Huawei récents, certains Tecno / Itel) : le GPS et le réseau d'Android directement.
 * Toute erreur est rattrapée : la localisation ne doit jamais faire planter l'application.
 */
class LocationSource(private val context: Context, private val onLocation: (Location) -> Unit) {

    val usesGoogle: Boolean = runCatching {
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }.getOrDefault(false)

    private val manager = context.getSystemService(LocationManager::class.java)
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(context) }
    private val fusedCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) = result.locations.forEach(onLocation)
    }
    private val androidListener = LocationListener { onLocation(it) }

    fun enabled(): Boolean = runCatching { LocationManagerCompat.isLocationEnabled(manager) }.getOrDefault(true)

    /** Fournisseurs de position disponibles, pour le diagnostic (« gps, network »). */
    fun providers(): String = runCatching { manager.getProviders(true).joinToString(", ") }.getOrDefault("?")

    @SuppressLint("MissingPermission")
    fun firstFix() {
        runCatching {
            if (usesGoogle) {
                fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { it?.let(onLocation) }
            } else {
                listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                    .mapNotNull { p -> runCatching { manager.getLastKnownLocation(p) }.getOrNull() }
                    .maxByOrNull { it.time }?.let(onLocation)
            }
        }
    }

    /** En course : précis, un point par 15 m. En attente : économe, un point par minute. */
    @SuppressLint("MissingPermission")
    fun start(delivering: Boolean) {
        stop()
        runCatching {
            if (usesGoogle) {
                val request = if (delivering) {
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000)
                        .setMinUpdateIntervalMillis(3_000).setMinUpdateDistanceMeters(15f)
                        .setMaxUpdateDelayMillis(15_000).setGranularity(Granularity.GRANULARITY_FINE).build()
                } else {
                    LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60_000)
                        .setMinUpdateIntervalMillis(30_000).build()
                }
                fused.requestLocationUpdates(request, fusedCallback, Looper.getMainLooper())
            } else {
                val interval = if (delivering) 5_000L else 60_000L
                val distance = if (delivering) 15f else 0f
                // Le réseau (antennes, Wi-Fi) répond vite, même à l'intérieur ; le GPS est plus précis dehors.
                for (p in listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)) {
                    if (runCatching { manager.isProviderEnabled(p) }.getOrDefault(false)) {
                        manager.requestLocationUpdates(p, interval, distance, androidListener, Looper.getMainLooper())
                    }
                }
            }
        }
    }

    fun stop() {
        runCatching { fused.removeLocationUpdates(fusedCallback) }
        runCatching { manager.removeUpdates(androidListener) }
    }
}
