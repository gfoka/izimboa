package com.izimboa.livreur.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.izimboa.livreur.data.Delivery
import com.izimboa.livreur.domain.FAILURE_REASONS
import com.izimboa.livreur.domain.NextAction
import com.izimboa.livreur.domain.Status
import com.izimboa.livreur.domain.digits
import com.izimboa.livreur.domain.fcfa
import com.izimboa.livreur.domain.headingToPickup
import com.izimboa.livreur.domain.nextAction

/** Écrans de la course en cours, une action principale à la fois. */
@Composable
fun DeliveryScreen(
    d: Delivery,
    busy: Boolean,
    message: String?,
    onPickUp: () -> Unit,
    onDepart: () -> Unit,
    onDeliver: (String) -> Unit,
    onFail: (String) -> Unit,
    onRetry: () -> Unit,
    onReturn: () -> Unit,
    onClose: () -> Unit,
    onDismiss: () -> Unit,
) {
    var mode by remember(d.id, d.status) { mutableStateOf(Mode.MAIN) }
    when {
        d.status == Status.DELIVERED || d.status == Status.RETURNED || d.status == Status.CANCELLED -> DoneScreen(d, onClose)
        mode == Mode.CODE -> CodeScreen(d, busy, message, onSubmit = onDeliver, onBack = { mode = Mode.MAIN; onDismiss() })
        mode == Mode.PROBLEM -> ProblemScreen(busy, onReason = { onFail(it); mode = Mode.MAIN }, onBack = { mode = Mode.MAIN })
        else -> MainStep(d, busy, message, onPickUp, onDepart, { mode = Mode.CODE }, { mode = Mode.PROBLEM }, onRetry, onReturn, onDismiss)
    }
}

private enum class Mode { MAIN, CODE, PROBLEM }

@Composable
private fun MainStep(
    d: Delivery, busy: Boolean, message: String?,
    onPickUp: () -> Unit, onDepart: () -> Unit, onEnterCode: () -> Unit, onProblem: () -> Unit,
    onRetry: () -> Unit, onReturn: () -> Unit, onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val toPickup = headingToPickup(d.status)
    val stop = if (toPickup) d.pickup else d.dropoff
    val phone = if (toPickup) d.pickupPhone else d.recipientPhone
    val who = if (toPickup) "Vendeur" else d.recipientName

    Column(Modifier.fillMaxSize().background(Brand.Cloud).statusBarsPadding().navigationBarsPadding()) {
        StepHeader(d)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card {
                Text(if (toPickup) "ALLEZ RAMASSER LE COLIS" else "ALLEZ LIVRER", style = MaterialTheme.typography.bodyMedium, color = Brand.Slate)
                Text(stop.address, style = MaterialTheme.typography.headlineMedium, color = Brand.Navy)
                if (stop.landmark != null) Text(stop.landmark, style = MaterialTheme.typography.titleMedium, color = Brand.Ink)
                Text(who, style = MaterialTheme.typography.bodyLarge, color = Brand.Slate)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    RoundAction(Icons.Filled.Call, "Appeler", Brand.Navy) { dial(context, phone) }
                    RoundAction(Icons.Filled.Chat, "WhatsApp", Brand.WhatsApp) { whatsapp(context, phone) }
                    RoundAction(Icons.Filled.Navigation, "Itinéraire", Brand.Sun) { navigate(context, stop.lat, stop.lng) }
                }
            }
            if (d.pricing.codAmount > 0 && d.status != Status.ASSIGNED) {
                Row(
                    Modifier.fillMaxWidth().background(Brand.SunSoft, RoundedCornerShape(20.dp)).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Payments, contentDescription = null, tint = Brand.Navy, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("À ENCAISSER", style = MaterialTheme.typography.bodyMedium, color = Brand.Slate)
                        Text(fcfa(d.pricing.codAmount), style = MaterialTheme.typography.headlineLarge, color = Brand.Navy)
                    }
                }
            }
            if (d.packageDescription != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(Icons.Filled.Inventory2, contentDescription = null, tint = Brand.Slate)
                    Spacer(Modifier.width(8.dp))
                    Text(d.packageDescription, style = MaterialTheme.typography.bodyLarge, color = Brand.Slate)
                }
            }
            if (d.status == Status.FAILED) {
                Banner("Échec signalé. Faites une nouvelle tentative ou rapportez le colis au vendeur.")
            }
            if (message != null) Banner(message, onClose = onDismiss)
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (nextAction(d.status)) {
                NextAction.PICK_UP -> BigButton(NextAction.PICK_UP.label, onPickUp, icon = Icons.Filled.Inventory2, loading = busy)
                NextAction.DEPART -> BigButton(NextAction.DEPART.label, onDepart, icon = Icons.Filled.TwoWheeler, loading = busy)
                NextAction.ENTER_CODE -> {
                    BigButton(NextAction.ENTER_CODE.label, onEnterCode, color = Brand.Go, contentColor = Color.White, icon = Icons.Filled.CheckCircle, enabled = !busy)
                    TextButton(onClick = onProblem, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Brand.Stop)
                        Spacer(Modifier.width(8.dp))
                        Text("Signaler un problème", style = MaterialTheme.typography.titleMedium, color = Brand.Stop)
                    }
                }
                NextAction.AFTER_FAILURE -> {
                    if (d.deliveryAttempts < 2) BigButton(NextAction.AFTER_FAILURE.label, onRetry, icon = Icons.Filled.TwoWheeler, loading = busy)
                    BigButton("RAPPORTER AU VENDEUR", onReturn, color = Brand.Navy, contentColor = Color.White, icon = Icons.Filled.Undo, enabled = !busy)
                }
                NextAction.NONE -> Unit
            }
        }
    }
}

/** En-tête : référence et les deux étapes (ramassage → livraison). */
@Composable
private fun StepHeader(d: Delivery) {
    val toPickup = headingToPickup(d.status)
    Column(Modifier.fillMaxWidth().background(Brand.Navy).padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(d.reference, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
            if (d.express) Pill("EXPRESS", Brand.Sun, Brand.Navy)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepDot("1", "Ramassage", active = toPickup, done = !toPickup)
            Box(Modifier.weight(1f).padding(horizontal = 8.dp).height(3.dp).background(if (toPickup) Color.White.copy(alpha = 0.25f) else Brand.Sun))
            StepDot("2", "Livraison", active = !toPickup, done = false)
        }
    }
}

@Composable
private fun StepDot(n: String, label: String, active: Boolean, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(32.dp).background(if (active || done) Brand.Sun else Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(if (done) "✓" else n, style = MaterialTheme.typography.titleMedium, color = Brand.Navy) }
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = if (active) Color.White else Color.White.copy(alpha = 0.6f))
    }
}

/** Saisie du code du destinataire : 4 grandes cases et un pavé numérique géant (utilisable avec un gant). */
@Composable
private fun CodeScreen(d: Delivery, busy: Boolean, message: String?, onSubmit: (String) -> Unit, onBack: () -> Unit) {
    var code by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Brand.Cloud).statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("← Retour", style = MaterialTheme.typography.titleMedium, color = Brand.Navy) }
        Text("Code du client", style = MaterialTheme.typography.headlineLarge, color = Brand.Navy)
        Text(
            if (d.pricing.codAmount > 0) "Encaissez ${fcfa(d.pricing.codAmount)}, puis demandez le code reçu sur WhatsApp."
            else "Demandez au client le code reçu sur WhatsApp.",
            style = MaterialTheme.typography.bodyLarge, color = Brand.Slate,
        )
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { i ->
                val c = code.getOrNull(i)
                Box(
                    Modifier.weight(1f).aspectRatio(0.85f).background(Color.White, RoundedCornerShape(18.dp))
                        .border(3.dp, if (i == code.length) Brand.Sun else Brand.Line, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text(c?.toString() ?: "", fontSize = 46.sp, style = MaterialTheme.typography.displayMedium, color = Brand.Navy) }
            }
        }
        if (message != null) {
            Spacer(Modifier.height(12.dp))
            Banner(message)
        }
        Spacer(Modifier.weight(1f))
        Keypad(
            onDigit = { if (code.length < 4) code += it },
            onDelete = { code = code.dropLast(1) },
        )
        Spacer(Modifier.height(12.dp))
        BigButton("VALIDER LA LIVRAISON", { onSubmit(code) }, color = Brand.Go, contentColor = Color.White,
            enabled = code.length == 4, loading = busy)
    }
}

@Composable
private fun Keypad(onDigit: (Char) -> Unit, onDelete: () -> Unit) {
    val rows = listOf("123", "456", "789", " 0<")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { k ->
                    val mod = Modifier.weight(1f).height(66.dp)
                    when (k) {
                        ' ' -> Spacer(mod)
                        '<' -> Button(onClick = onDelete, modifier = mod, shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Brand.Line, contentColor = Brand.Navy)) {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Effacer", modifier = Modifier.size(30.dp))
                        }
                        else -> Button(onClick = { onDigit(k) }, modifier = mod, shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Brand.Navy)) {
                            Text(k.toString(), fontSize = 30.sp, style = MaterialTheme.typography.headlineLarge)
                        }
                    }
                }
            }
        }
    }
}

/** Motif d'échec en un appui. */
@Composable
private fun ProblemScreen(busy: Boolean, onReason: (String) -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Brand.Cloud).statusBarsPadding().navigationBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Retour", style = MaterialTheme.typography.titleMedium, color = Brand.Navy) }
        Text("Que se passe-t-il ?", style = MaterialTheme.typography.headlineLarge, color = Brand.Navy)
        Text("Votre position est enregistrée avec le signalement.", style = MaterialTheme.typography.bodyLarge, color = Brand.Slate)
        Spacer(Modifier.height(4.dp))
        FAILURE_REASONS.forEach { r -> SecondaryButton(r, { onReason(r) }, color = Brand.Stop, enabled = !busy) }
    }
}

@Composable
private fun DoneScreen(d: Delivery, onClose: () -> Unit) {
    val ok = d.status == Status.DELIVERED
    Column(
        Modifier.fillMaxSize().background(if (ok) Brand.Go else Brand.Navy).statusBarsPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Icon(if (ok) Icons.Filled.CheckCircle else Icons.Filled.Undo, contentDescription = null, tint = Color.White, modifier = Modifier.size(120.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            when (d.status) {
                Status.DELIVERED -> "Livraison réussie !"
                Status.RETURNED -> "Colis rapporté au vendeur"
                else -> "Course annulée"
            },
            style = MaterialTheme.typography.displayMedium, color = Color.White, textAlign = TextAlign.Center,
        )
        if (ok && d.pricing.codAmount > 0) {
            Spacer(Modifier.height(12.dp))
            Text("Vous avez encaissé ${fcfa(d.pricing.codAmount)}.\nGardez-le pour le reversement du soir.",
                style = MaterialTheme.typography.titleMedium, color = Color.White, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.weight(1f))
        BigButton("TERMINER", onClose, color = Color.White, contentColor = if (ok) Brand.Go else Brand.Navy)
    }
}

// ---- Applications du téléphone ----

private fun dial(context: Context, phone: String) =
    runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }

private fun whatsapp(context: Context, phone: String) =
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${digits(phone)}"))) }

/** Itinéraire en mode deux-roues dans Google Maps, sinon n'importe quelle application de carte. */
private fun navigate(context: Context, lat: Double, lng: Double) {
    val maps = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$lat,$lng&mode=l")).setPackage("com.google.android.apps.maps")
    if (runCatching { context.startActivity(maps) }.isFailure) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng"))) }
    }
}
