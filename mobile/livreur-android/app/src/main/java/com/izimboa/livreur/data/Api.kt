package com.izimboa.livreur.data

import com.izimboa.livreur.BuildConfig
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.coroutines.executeAsync

/** Erreur lisible par le livreur (message en français venant de l'API, ou générique). */
class ApiException(val status: Int, val code: String?, message: String) : Exception(message)

/**
 * Client de l'API Izimboa. Chaque appel demande un jeton frais (renouvelé automatiquement).
 * Réseau mobile instable : délais courts, l'appelant réessaie au tour suivant.
 */
class Api(private val token: suspend () -> String?) {

    val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val jsonType = "application/json".toMediaType()

    suspend fun status(): CourierStatus = call("GET", "/v1/delivery/courier/status")!!
    suspend fun online(at: PointBody?): CourierStatus = call("POST", "/v1/delivery/courier/online", json.encodeToString(OnlineBody(at)))!!
    suspend fun offline(): CourierStatus = call("POST", "/v1/delivery/courier/offline", "{}")!!
    suspend fun positions(points: List<PointBody>) { call<Unit>("POST", "/v1/delivery/courier/positions", json.encodeToString(PositionsBody(points)), parse = false) }

    suspend fun currentOffer(): Offer? = call("GET", "/v1/delivery/courier/offers/current")
    suspend fun accept(offerId: String): Accepted = call("POST", "/v1/delivery/courier/offers/$offerId/accept", "{}")!!
    suspend fun decline(offerId: String) { call<Unit>("POST", "/v1/delivery/courier/offers/$offerId/decline", "{}", parse = false) }

    suspend fun assigned(): List<DeliverySummary> = call("GET", "/v1/delivery/deliveries?scope=ASSIGNED")!!
    suspend fun delivery(id: String): Delivery = call("GET", "/v1/delivery/deliveries/$id")!!

    suspend fun pickUp(id: String, at: LatLngBody): Delivery = step(id, "pickup", json.encodeToString(at))
    suspend fun depart(id: String, at: LatLngBody): Delivery = step(id, "depart", json.encodeToString(at))
    suspend fun deliver(id: String, body: CodeBody): Delivery = step(id, "deliver", json.encodeToString(body))
    suspend fun fail(id: String, body: ReasonBody): Delivery = step(id, "fail", json.encodeToString(body))
    suspend fun retry(id: String, at: LatLngBody): Delivery = step(id, "retry", json.encodeToString(at))
    suspend fun returnToSender(id: String, at: LatLngBody): Delivery = step(id, "return", json.encodeToString(at))

    private suspend fun step(id: String, action: String, body: String): Delivery =
        call("POST", "/v1/delivery/deliveries/$id/$action", body)!!

    /** Retourne null pour une réponse 204 (rien à montrer). */
    private suspend inline fun <reified T> call(method: String, path: String, body: String? = null, parse: Boolean = true): T? {
        val req = Request.Builder()
            .url(BuildConfig.API_URL + path)
            .header("Accept", "application/json")
            .apply { token()?.let { header("Authorization", "Bearer $it") } }
            .method(method, body?.toRequestBody(jsonType))
            .build()
        val response = try {
            http.newCall(req).executeAsync()
        } catch (e: IOException) {
            throw ApiException(0, "network", "Pas de réseau. Réessayez dans un instant.")
        }
        response.use { res ->
            val text = res.body.string()
            if (res.isSuccessful) {
                if (res.code == 204 || !parse || text.isBlank()) return null
                return json.decodeFromString<T>(text)
            }
            val problem = runCatching { json.parseToJsonElement(text) as JsonObject }.getOrNull()
            val code = problem?.get("code")?.jsonPrimitive?.content
            val detail = problem?.get("detail")?.jsonPrimitive?.content
            throw ApiException(res.code, code, when {
                code != null && detail != null -> detail
                res.code == 401 -> "Session expirée : reconnectez-vous."
                res.code == 403 -> "Votre compte n'est pas encore activé comme livreur."
                res.code == 404 -> "Course introuvable."
                res.code == 409 -> "Action déjà faite ou plus possible. Actualisez."
                else -> "Le service ne répond pas. Réessayez."
            })
        }
    }
}
