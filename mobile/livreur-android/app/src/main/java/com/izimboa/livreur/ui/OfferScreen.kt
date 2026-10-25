package com.izimboa.livreur.ui

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.izimboa.livreur.data.Offer
import com.izimboa.livreur.domain.distance
import com.izimboa.livreur.domain.fcfa
import kotlinx.coroutines.delay

/**
 * Course proposée, en plein écran : le gain en énorme, où ramasser, où livrer,
 * et deux gros boutons. Sonne et vibre jusqu'à la réponse ou la fin du compte à rebours.
 */
@Composable
fun OfferScreen(offer: Offer, receivedAt: Long, busy: Boolean, onAccept: () -> Unit, onDecline: () -> Unit, onExpired: () -> Unit) {
    var left by remember(offer.offerId) { mutableIntStateOf(remaining(offer, receivedAt)) }
    LaunchedEffect(offer.offerId) {
        while (left > 0) {
            delay(1000)
            left = remaining(offer, receivedAt)
        }
        onExpired()
    }
    Ring(offer.offerId)

    val d = offer.delivery
    Column(Modifier.fillMaxSize().background(Brand.Navy).statusBarsPadding().navigationBarsPadding().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("NOUVELLE COURSE", style = MaterialTheme.typography.titleMedium, color = Brand.Sun)
                if (d.express) {
                    Spacer(Modifier.height(6.dp))
                    Pill("EXPRESS", Brand.Sun, Brand.Navy)
                }
            }
            Countdown(left, offer.secondsLeft.coerceAtLeast(1))
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Spacer(Modifier.height(8.dp))
            Text("Vous gagnez", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
            Text(fcfa(offer.courierEarning), style = MaterialTheme.typography.displayLarge, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Fact(Icons.Filled.TwoWheeler, "${distance(offer.distanceToPickupMeters)} jusqu'au colis")
            }
            Fact(Icons.Filled.Place, "Course de ${distance(d.distanceMeters)}")

            Stop("RAMASSAGE", d.pickupAddress, d.pickupLandmark, Brand.Sun)
            Stop("LIVRAISON", d.dropoffAddress, d.dropoffLandmark, Color.White)

            if (d.codAmount > 0) {
                Row(
                    Modifier.fillMaxWidth().background(Brand.SunSoft, androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Payments, contentDescription = null, tint = Brand.Navy, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("À encaisser : ${fcfa(d.codAmount)}", style = MaterialTheme.typography.titleLarge, color = Brand.Navy)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        BigButton("ACCEPTER", onAccept, color = Brand.Go, contentColor = Color.White, icon = Icons.Filled.Check, loading = busy)
        Spacer(Modifier.height(10.dp))
        SecondaryButton("Refuser", onDecline, color = Color.White, enabled = !busy)
    }
}

private fun remaining(offer: Offer, receivedAt: Long): Int =
    (offer.secondsLeft - ((System.currentTimeMillis() - receivedAt) / 1000).toInt()).coerceAtLeast(0)

@Composable
private fun Countdown(left: Int, total: Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
        CircularProgressIndicator(
            progress = { left / total.toFloat() },
            modifier = Modifier.size(76.dp), color = if (left <= 10) Brand.Stop else Brand.Sun,
            trackColor = Color.White.copy(alpha = 0.15f), strokeWidth = 7.dp,
        )
        Text("$left", style = MaterialTheme.typography.headlineMedium, color = Color.White)
    }
}

@Composable
private fun Fact(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Brand.Sun, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

@Composable
private fun Stop(label: String, address: String, landmark: String?, dot: Color) {
    Row {
        Box(Modifier.padding(top = 6.dp).size(14.dp).background(dot, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.65f))
            Text(address, style = MaterialTheme.typography.titleLarge, color = Color.White)
            if (landmark != null) Text(landmark, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

/** Sonnerie + vibration en boucle tant que l'écran de proposition est affiché. */
@Composable
private fun Ring(key: String) {
    if (LocalInspectionMode.current) return
    val context = LocalContext.current
    DisposableEffect(key) {
        val ringtone = runCatching {
            RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                ?.apply { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLooping = true; play() }
        }.getOrNull()
        val vibrator = vibrator(context)
        runCatching { vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 700, 500), 0)) }
        onDispose {
            runCatching { ringtone?.stop() }
            runCatching { vibrator.cancel() }
        }
    }
}

@Suppress("DEPRECATION")
private fun vibrator(context: Context): Vibrator =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) context.getSystemService(VibratorManager::class.java).defaultVibrator
    else context.getSystemService(Vibrator::class.java)
