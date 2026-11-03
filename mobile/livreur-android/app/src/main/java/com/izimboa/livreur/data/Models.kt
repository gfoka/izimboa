package com.izimboa.livreur.data

import com.izimboa.livreur.domain.Status
import kotlinx.serialization.Serializable

/** Formats de l'API Delivery (api.izimboa.com), limités à ce dont l'application livreur a besoin. */

@Serializable
data class LatLng(val lat: Double, val lng: Double)

@Serializable
data class CourierStatus(val online: Boolean, val onlineSince: String? = null, val lastPosition: Position? = null)

@Serializable
data class Position(val lat: Double, val lng: Double, val accuracyMeters: Float? = null, val at: String? = null)

@Serializable
data class OfferDelivery(
    val id: String,
    val reference: String,
    val express: Boolean,
    val pickupLat: Double,
    val pickupLng: Double,
    val pickupAddress: String,
    val pickupLandmark: String? = null,
    val dropoffAddress: String,
    val dropoffLandmark: String? = null,
    val distanceMeters: Int,
    val courseFee: Long,
    val codAmount: Long,
)

@Serializable
data class Offer(
    val offerId: String,
    val secondsLeft: Int,
    val distanceToPickupMeters: Int,
    val courierEarning: Long,
    val delivery: OfferDelivery,
)

@Serializable
data class Accepted(val deliveryId: String)

@Serializable
data class DeliverySummary(val id: String, val reference: String, val status: Status)

@Serializable
data class Stop(val lat: Double, val lng: Double, val address: String, val landmark: String? = null)

@Serializable
data class Pricing(val codAmount: Long, val totalFee: Long, val feePayment: String)

@Serializable
data class Delivery(
    val id: String,
    val reference: String,
    val status: Status,
    val pickup: Stop,
    val pickupPhone: String,
    val dropoff: Stop,
    val recipientName: String,
    val recipientPhone: String,
    val packageDescription: String? = null,
    val express: Boolean,
    val distanceKm: Double,
    val pricing: Pricing,
    val handoverFailures: Int = 0,
    val deliveryAttempts: Int = 0,
)

@Serializable
data class PointBody(
    val lat: Double,
    val lng: Double,
    val accuracy: Float? = null,
    val speed: Float? = null,
    val heading: Float? = null,
    val recordedAt: String,
)

@Serializable
data class PositionsBody(val points: List<PointBody>)

@Serializable
data class OnlineBody(val position: PointBody? = null)

/** occurredAt : heure réelle de l'action (elle peut partir plus tard, après une coupure réseau). */
@Serializable
data class CodeBody(val code: String, val lat: Double? = null, val lng: Double? = null, val occurredAt: String? = null)

@Serializable
data class ReasonBody(val reason: String, val lat: Double? = null, val lng: Double? = null, val occurredAt: String? = null)

@Serializable
data class LatLngBody(val lat: Double? = null, val lng: Double? = null, val occurredAt: String? = null)

// ---- Dossier du livreur (décision 74) ----

@Serializable
data class DocPage(val id: Int, val name: String, val camera: String)

@Serializable
data class DocType(
    val id: Int, val code: String, val name: String, val help: String? = null, val mandatory: Boolean,
    val numberRequirement: String, val expiryRequired: Boolean, val pages: List<DocPage> = emptyList(),
)

@Serializable
data class DocSubmitted(val id: String, val number: String? = null, val expiresOn: String? = null, val pages: List<Int> = emptyList())

/** state : MISSING, EXPIRED, EXPIRING_SOON, OK. */
@Serializable
data class DocRequirement(val type: DocType, val submitted: DocSubmitted? = null, val state: String)

/** status : DRAFT, PENDING, ACTIVE, REJECTED, SUSPENDED. */
@Serializable
data class MyFile(
    val status: String, val reason: String? = null, val firstName: String? = null, val lastName: String? = null,
    val email: String? = null, val phone: String? = null, val documents: List<DocRequirement> = emptyList(),
    val complete: Boolean, val canWork: Boolean, val deadline: String? = null,
)

@Serializable
data class ProfileBody(val firstName: String, val lastName: String, val email: String?)
