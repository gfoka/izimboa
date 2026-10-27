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
import com.izimboa.livreur.domain.Status
import com.izimboa.livreur.domain.isActive
import com.izimboa.livreur.offline.LocalStore
import com.izimboa.livreur.offline.Sync
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * État du livreur partagé entre l'écran et le service en arrière-plan.
 *
 * Hors connexion d'abord (décision n° 62) : chaque action (ramassé, parti, échec, retour…) est d'abord
 * écrite dans la base chiffrée du téléphone, l'écran avance tout de suite, puis l'action part au serveur
 * dès que possible, dans l'ordre, avec son heure réelle. Seuls les gestes qui demandent le serveur
 * (passer en ligne, accepter une course, valider le code du client) exigent du réseau.
 */
class Courier(private val context: Context, private val api: Api, private val store: LocalStore) {

    data class State(
        val online: Boolean = false,
        val busy: Boolean = false,          // une action réseau est en cours (boutons désactivés)
        val offer: Offer? = null,
        val offerReceivedAt: Long = 0,      // horloge du téléphone, pour le compte à rebours
        val delivery: Delivery? = null,
        val message: String? = null,
        val info: String? = null,           // message positif (ex. « enregistré, envoi au retour du réseau »)
        val lastLocation: Location? = null,
        val pendingActions: Long = 0,
        // Indicateur affiché quand le livreur est en ligne (diagnostic sur le terrain).
        val gpsAt: Long? = null,            // dernier point GPS obtenu (horloge du téléphone)
        val sentAt: Long? = null,           // dernier envoi de positions réussi
        val sendError: String? = null,
        val locationEnabled: Boolean = true,
    )

    private val json = api.json
    private val prefs = context.getSharedPreferences("courier", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(
        State(
            online = prefs.getBoolean(KEY_ONLINE, false),
            delivery = store.get(KEY_DELIVERY)?.let { runCatching { json.decodeFromString<Delivery>(it) }.getOrNull() },
            pendingActions = store.pendingActions(),
        ),
    )
    val state: StateFlow<State> = _state

    lateinit var sync: Sync

    fun onPositionsSent(error: String?) = _state.update {
        if (error == null) it.copy(sentAt = System.currentTimeMillis(), sendError = null) else it.copy(sendError = error)
    }

    fun onLocationSettings(enabled: Boolean) = _state.update { it.copy(locationEnabled = enabled) }

    fun clearMessage() = _state.update { it.copy(message = null, info = null) }

    // ---- Positions ----

    private var lastStoredAt = 0L

    /** Chaque position est d'abord gardée sur le téléphone. */
    fun recordLocation(l: Location) {
        store.addPosition(point(l))
        lastStoredAt = System.currentTimeMillis()
        _state.update { it.copy(lastLocation = l, gpsAt = System.currentTimeMillis()) }
    }

    /**
     * Livreur immobile en attente de course : le GPS ne produit plus de point (économie de batterie).
     * On redonne sa dernière position, sans rallumer le GPS, pour qu'il reste visible et reçoive des courses.
     */
    fun heartbeat(everyMs: Long) {
        val l = _state.value.lastLocation ?: return
        if (System.currentTimeMillis() - lastStoredAt < everyMs) return
        store.addPosition(point(l).copy(recordedAt = Instant.now().toString(), speed = 0f))
        lastStoredAt = System.currentTimeMillis()
    }

    // ---- En ligne / hors ligne ----

    suspend fun goOnline() = act {
        // Un « hors ligne » resté en attente doit partir avant, sinon il annulerait ce passage en ligne.
        if (!sendNow()) throw ApiException(0, "network", "Pas de réseau. Activez vos données pour passer en ligne.")
        val l = _state.value.lastLocation
        api.online(l?.let { point(it) })
        setOnline(true)
        ContextCompat.startForegroundService(context, Intent(context, TrackingService::class.java))
        refreshDelivery()
    }

    /** Hors ligne : possible même sans réseau (le serveur sera prévenu au retour du réseau, après les positions). */
    suspend fun goOffline() {
        context.stopService(Intent(context, TrackingService::class.java))
        setOnline(false)
        _state.update { it.copy(offer = null) }
        store.enqueue(Sync.OFFLINE, null, "{}")
        sendNow()
    }

    /** Au retour dans l'application : on envoie ce qui attend, puis on se recale sur le serveur. */
    suspend fun sync() {
        sendNow()
        runCatching {
            val s = api.status()
            if (store.pendingActions() == 0L) setOnline(s.online)
            if (_state.value.online) ContextCompat.startForegroundService(context, Intent(context, TrackingService::class.java))
            refreshDelivery()
        }
    }

    private fun setOnline(online: Boolean) {
        prefs.edit().putBoolean(KEY_ONLINE, online).apply()
        _state.update { it.copy(online = online) }
    }

    // ---- Propositions de course ----

    /** Appelé par le service (attente longue). Retourne la proposition si elle est nouvelle (pour faire sonner). */
    suspend fun waitForOffer(seconds: Int): Offer? {
        if (_state.value.delivery != null) return null
        val offer = api.currentOffer(seconds)
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
        _state.update { it.copy(offer = null) }
        setDelivery(api.delivery(accepted.deliveryId))
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

    // ---- Course en cours : actions enregistrées sur le téléphone puis envoyées ----

    suspend fun pickUp() = queue("pickup", Status.PICKED_UP)
    suspend fun depart() = queue("depart", Status.IN_TRANSIT)
    suspend fun retry() = queue("retry", Status.IN_TRANSIT)
    suspend fun returnToSender() = queue("return", Status.RETURNED)
    suspend fun fail(reason: String) = queue("fail", Status.FAILED, reason)

    private suspend fun queue(kind: String, next: Status, reason: String? = null) {
        val d = _state.value.delivery ?: return
        val l = _state.value.lastLocation
        val now = Instant.now().toString()
        val body = if (reason != null) json.encodeToString(ReasonBody(reason, l?.latitude, l?.longitude, now))
        else json.encodeToString(LatLngBody(l?.latitude, l?.longitude, now))
        store.enqueue(kind, d.id, body)
        setDelivery(
            d.copy(
                status = next,
                deliveryAttempts = d.deliveryAttempts + if (kind == "depart" || kind == "retry") 1 else 0,
            ),
            local = true,
        )
        if (!sendNow()) {
            _state.update { it.copy(info = "Pas de réseau : c'est enregistré sur le téléphone et partira tout seul.") }
        }
    }

    /**
     * Remise : le code du client est vérifié par le serveur (protection contre la fraude),
     * donc il faut du réseau, quelques secondes suffisent. Les actions en attente partent d'abord.
     */
    suspend fun deliver(code: String) = act {
        val d = _state.value.delivery ?: return@act
        if (!sendNow()) throw ApiException(0, "network", NEED_NETWORK)
        val l = _state.value.lastLocation
        try {
            setDelivery(api.deliver(d.id, CodeBody(code, l?.latitude, l?.longitude, Instant.now().toString())))
        } catch (e: ApiException) {
            throw if (e.status == 0) ApiException(0, "network", NEED_NETWORK) else e
        }
    }

    suspend fun refreshDelivery() {
        if (store.pendingActions() > 0) return // le téléphone est en avance sur le serveur : on garde son état
        val active = api.assigned().firstOrNull { isActive(it.status) }
        val fresh = active?.let { api.delivery(it.id) }
        val current = _state.value.delivery
        // Une course qui vient de se terminer reste affichée (écran « bravo ») jusqu'à ce que le livreur la ferme.
        if (current != null && !isActive(current.status)) return
        setDelivery(fresh)
    }

    /** Fin de course affichée : on revient à l'accueil. */
    fun closeFinishedDelivery() = setDelivery(null)

    /** Réponse du serveur à une action envoyée depuis la file : l'état du serveur fait foi s'il ne reste rien à envoyer. */
    fun onServerDelivery(d: Delivery) {
        if (store.pendingActions() <= 1 && _state.value.delivery?.id == d.id) setDelivery(d)
    }

    fun onRejected(message: String) = _state.update { it.copy(message = message) }

    private fun setDelivery(d: Delivery?, local: Boolean = false) {
        store.put(KEY_DELIVERY, d?.let { json.encodeToString(it) })
        _state.update { it.copy(delivery = d, pendingActions = store.pendingActions(), info = if (local) it.info else null) }
    }

    private suspend fun sendNow(): Boolean {
        val ok = runCatching { sync.flush() }.getOrDefault(false)
        _state.update { it.copy(pendingActions = store.pendingActions()) }
        return ok
    }

    /** Action qui exige le serveur : boutons bloqués pendant l'appel, message clair en cas d'échec. */
    private suspend fun act(block: suspend () -> Unit) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, message = null, info = null) }
        try {
            block()
        } catch (e: ApiException) {
            _state.update { it.copy(message = e.message) }
            if (e.code == "invalid-transition" || e.status == 404) runCatching { refreshDelivery() }
        } finally {
            _state.update { it.copy(busy = false) }
        }
    }

    companion object {
        private const val KEY_ONLINE = "online"
        private const val KEY_DELIVERY = "delivery"
        const val NEED_NETWORK = "Pas de réseau : le code du client doit être vérifié en ligne. Activez vos données quelques secondes, puis validez."

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
