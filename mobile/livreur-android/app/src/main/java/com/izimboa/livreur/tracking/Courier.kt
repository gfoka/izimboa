package com.izimboa.livreur.tracking

import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.core.content.ContextCompat
import com.izimboa.livreur.data.Api
import com.izimboa.livreur.data.ApiException
import com.izimboa.livreur.data.CodeBody
import com.izimboa.livreur.data.Delivery
import com.izimboa.livreur.data.LatLngBody
import com.izimboa.livreur.data.Offer
import com.izimboa.livreur.data.PointBody
import com.izimboa.livreur.data.ReasonBody
import com.izimboa.livreur.domain.isActive
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * État du livreur partagé entre l'écran et le service en arrière-plan :
 * en ligne ou non, course proposée, course en cours, message à afficher.
 */
class Courier(private val context: Context, private val api: Api) {

    data class State(
        val online: Boolean = false,
        val busy: Boolean = false,          // une action réseau est en cours (boutons désactivés)
        val offer: Offer? = null,
        val offerReceivedAt: Long = 0,      // horloge du téléphone, pour le compte à rebours
        val delivery: Delivery? = null,
        val message: String? = null,
        val lastLocation: Location? = null,
    )

    private val prefs = context.getSharedPreferences("courier", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(State(online = prefs.getBoolean(KEY_ONLINE, false)))
    val state: StateFlow<State> = _state

    fun clearMessage() = _state.update { it.copy(message = null) }
    fun onLocation(l: Location) = _state.update { it.copy(lastLocation = l) }

    // ---- En ligne / hors ligne ----

    suspend fun goOnline() = act {
        val l = _state.value.lastLocation
        api.online(l?.let { point(it) })
        setOnline(true)
        ContextCompat.startForegroundService(context, Intent(context, TrackingService::class.java))
        refreshDelivery()
    }

    suspend fun goOffline() = act {
        api.offline()
        setOnline(false)
        context.stopService(Intent(context, TrackingService::class.java))
        _state.update { it.copy(offer = null) }
    }

    /** Au retour dans l'application : on se recale sur le serveur (source de vérité). */
    suspend fun sync() {
        runCatching {
            val s = api.status()
            setOnline(s.online)
            if (s.online) ContextCompat.startForegroundService(context, Intent(context, TrackingService::class.java))
            refreshDelivery()
        }
    }

    private fun setOnline(online: Boolean) {
        prefs.edit().putBoolean(KEY_ONLINE, online).apply()
        _state.update { it.copy(online = online) }
    }

    // ---- Propositions de course ----

    /** Appelé par le service. Retourne la proposition si elle est nouvelle (pour faire sonner). */
    suspend fun pollOffer(): Offer? {
        if (_state.value.delivery != null) return null
        val offer = api.currentOffer()
        val previous = _state.value.offer
        _state.update {
            if (offer == null) it.copy(offer = null)
            else if (previous?.offerId == offer.offerId) it
            else it.copy(offer = offer, offerReceivedAt = System.currentTimeMillis())
        }
        return offer.takeIf { it != null && it.offerId != previous?.offerId }
    }

    suspend fun accept() = act {
        val offer = _state.value.offer ?: return@act
        val accepted = api.accept(offer.offerId)
        _state.update { it.copy(offer = null, delivery = api.delivery(accepted.deliveryId)) }
        Notifications.cancelOffer(context)
    }

    suspend fun decline() = act {
        val offer = _state.value.offer ?: return@act
        _state.update { it.copy(offer = null) }
        Notifications.cancelOffer(context)
        api.decline(offer.offerId)
    }

    /** Le délai est passé : la course part chez un autre livreur. */
    fun offerExpired() {
        _state.update { it.copy(offer = null, message = "Course partie chez un autre livreur.") }
        Notifications.cancelOffer(context)
    }

    // ---- Course en cours ----

    suspend fun refreshDelivery() {
        val active = api.assigned().firstOrNull { isActive(it.status) }
        val fresh = active?.let { api.delivery(it.id) }
        // Une course qui vient de se terminer reste affichée (écran « bravo ») jusqu'à ce que le livreur la ferme.
        _state.update { s -> if (s.delivery != null && !isActive(s.delivery.status)) s else s.copy(delivery = fresh) }
    }

    suspend fun pickUp() = step { api.pickUp(it.id, here()) }
    suspend fun depart() = step { api.depart(it.id, here()) }
    suspend fun retry() = step { api.retry(it.id, here()) }
    suspend fun returnToSender() = step { api.returnToSender(it.id, here()) }
    suspend fun fail(reason: String) = step { d -> here().let { api.fail(d.id, ReasonBody(reason, it.lat, it.lng)) } }

    /** Retourne vrai si le code est bon (livraison terminée). */
    suspend fun deliver(code: String): Boolean {
        var ok = false
        step { d -> here().let { api.deliver(d.id, CodeBody(code, it.lat, it.lng)) }.also { ok = true } }
        return ok
    }

    /** Fin de course affichée : on revient à l'accueil. */
    fun closeFinishedDelivery() = _state.update { it.copy(delivery = null) }

    private suspend fun step(call: suspend (Delivery) -> Delivery) = act {
        val current = _state.value.delivery ?: return@act
        val updated = call(current)
        _state.update { it.copy(delivery = updated) }
    }

    /** Exécute une action réseau : boutons bloqués pendant l'appel, message clair en cas d'échec. */
    private suspend fun act(block: suspend () -> Unit) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, message = null) }
        try {
            block()
        } catch (e: ApiException) {
            _state.update { it.copy(message = e.message) }
            if (e.code == "invalid-transition" || e.status == 404) runCatching { refreshDelivery() }
        } finally {
            _state.update { it.copy(busy = false) }
        }
    }

    private fun here(): LatLngBody = _state.value.lastLocation.let { LatLngBody(it?.latitude, it?.longitude) }

    companion object {
        private const val KEY_ONLINE = "online"

        fun point(l: Location) = PointBody(
            lat = l.latitude,
            lng = l.longitude,
            accuracy = if (l.hasAccuracy()) l.accuracy else null,
            speed = if (l.hasSpeed()) l.speed else null,
            heading = if (l.hasBearing()) l.bearing else null,
            recordedAt = Instant.ofEpochMilli(l.time).toString(),
        )
    }
}
