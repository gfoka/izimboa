package com.izimboa.livreur.domain

import java.text.NumberFormat
import java.util.Base64
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Logique pure (sans Android) : testée sur la JVM. */

enum class Status { CREATED, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED, RETURNED, CANCELLED }

/** L'unique action principale proposée au livreur à chaque étape (« une seule action à la fois »). */
enum class NextAction(val label: String) {
    PICK_UP("J'AI RAMASSÉ LE COLIS"),
    DEPART("JE PARS LIVRER"),
    ENTER_CODE("ARRIVÉ : SAISIR LE CODE"),
    AFTER_FAILURE("NOUVELLE TENTATIVE"),
    NONE(""),
}

fun nextAction(status: Status): NextAction = when (status) {
    Status.ASSIGNED -> NextAction.PICK_UP
    Status.PICKED_UP -> NextAction.DEPART
    Status.IN_TRANSIT -> NextAction.ENTER_CODE
    Status.FAILED -> NextAction.AFTER_FAILURE
    else -> NextAction.NONE
}

/** Vers où le livreur roule : le vendeur avant le ramassage, le destinataire ensuite. */
fun headingToPickup(status: Status): Boolean = status == Status.ASSIGNED

fun isActive(status: Status): Boolean =
    status == Status.ASSIGNED || status == Status.PICKED_UP || status == Status.IN_TRANSIT || status == Status.FAILED

/** Motifs d'échec proposés en gros boutons (pas de saisie au guidon). */
val FAILURE_REASONS = listOf(
    "Destinataire absent",
    "Destinataire injoignable",
    "Refuse le colis",
    "Adresse introuvable",
    "Pas l'argent pour payer",
)

private val money = NumberFormat.getIntegerInstance(Locale.FRANCE)

/** 12775 → « 12 775 FCFA » (espaces normales : lisibles partout). */
fun fcfa(amount: Long): String = money.format(amount).replace(' ', ' ').replace(' ', ' ') + " FCFA"

/** 1450 m → « 1,5 km » ; 600 m → « 600 m ». */
fun distance(meters: Int): String =
    if (meters < 1000) "$meters m" else String.format(Locale.FRANCE, "%.1f km", meters / 1000.0)

/** « +237 699 12 34 56 » → « 237699123456 » pour les liens wa.me. */
fun digits(phone: String): String = phone.filter { it.isDigit() }

/** Rôles du realm Keycloak lus dans le jeton d'accès (partie centrale du JWT). */
fun realmRoles(accessToken: String?): Set<String> {
    val payload = accessToken?.split('.')?.getOrNull(1) ?: return emptySet()
    return try {
        val json = String(Base64.getUrlDecoder().decode(payload.padEnd((payload.length + 3) / 4 * 4, '=')))
        val roles = (Json.parseToJsonElement(json) as JsonObject)["realm_access"]
            ?.let { (it as JsonObject)["roles"] as? JsonArray } ?: return emptySet()
        roles.map { it.jsonPrimitive.content }.toSet()
    } catch (e: IllegalArgumentException) {
        emptySet()
    }
}
