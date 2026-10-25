package com.izimboa.livreur.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.izimboa.livreur.R

@Composable
fun LoginScreen(onLogin: () -> Unit, message: String?) {
    Column(
        Modifier.fillMaxSize().background(Brand.Navy).statusBarsPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Image(painterResource(R.drawable.ic_symbol), contentDescription = null, modifier = Modifier.size(120.dp))
        Spacer(Modifier.height(20.dp))
        Text("Izimboa Livreur", style = MaterialTheme.typography.displayMedium, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(
            "Recevez des courses près de vous.\nGagnez plus, chaque jour.",
            style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        if (message != null) {
            Banner(message)
            Spacer(Modifier.height(16.dp))
        }
        BigButton("SE CONNECTER", onLogin)
        Spacer(Modifier.height(12.dp))
        Text(
            "Avec votre compte Izimboa (WhatsApp, Google ou e-mail).",
            style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun NotCourierScreen(name: String?, onLogout: () -> Unit, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Brand.Cloud).statusBarsPadding().navigationBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.weight(1f))
        Text("Bonjour${name?.let { " $it" } ?: ""} 👋", style = MaterialTheme.typography.headlineLarge, color = Brand.Navy)
        Text(
            "Votre compte n'est pas encore activé comme livreur. Après la vérification de vos documents, Izimboa l'active et vous pourrez recevoir des courses.",
            style = MaterialTheme.typography.bodyLarge, color = Brand.Ink,
        )
        Spacer(Modifier.weight(1f))
        BigButton("J'AI ÉTÉ ACTIVÉ, RÉESSAYER", onRetry)
        SecondaryButton("Se déconnecter", onLogout)
    }
}

/** Accueil : un énorme bouton rond pour passer en ligne. Rien d'autre à comprendre. */
@Composable
fun HomeScreen(name: String?, online: Boolean, busy: Boolean, message: String?, onToggle: () -> Unit, onLogout: () -> Unit, onDismiss: () -> Unit, info: String? = null) {
    Column(Modifier.fillMaxSize().background(if (online) Brand.Navy else Brand.Cloud).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Bonjour${name?.let { " $it" } ?: ""}", style = MaterialTheme.typography.headlineMedium, color = if (online) Color.White else Brand.Navy)
                Text(
                    if (online) "Vous êtes en ligne" else "Vous êtes hors ligne",
                    style = MaterialTheme.typography.bodyLarge, color = if (online) Brand.Sun else Brand.Slate,
                )
            }
            IconButton(onClick = onLogout, enabled = !online, modifier = Modifier.size(56.dp)) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Se déconnecter",
                    tint = if (online) Color.White.copy(alpha = 0.3f) else Brand.Slate)
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            PowerButton(online, busy, onToggle)
        }

        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (message != null) Banner(message, onClose = onDismiss)
            else if (info != null) Banner(info, error = false, onClose = onDismiss)
            Text(
                if (online) "Gardez l'application ouverte ou en arrière-plan. Une course qui arrive sonne comme un appel."
                else "Passez en ligne pour recevoir des courses près de vous. Votre position n'est partagée que lorsque vous êtes en ligne.",
                style = MaterialTheme.typography.bodyLarge,
                color = if (online) Color.White.copy(alpha = 0.85f) else Brand.Slate,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PowerButton(online: Boolean, busy: Boolean, onToggle: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f, targetValue = if (online) 1.08f else 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "scale",
    )
    val color = if (online) Brand.Go else Brand.Offline
    Box(contentAlignment = Alignment.Center) {
        Box(Modifier.size(260.dp).scale(pulse).clip(CircleShape).background(color.copy(alpha = 0.18f)))
        Column(
            Modifier.size(224.dp).clip(CircleShape).background(color).border(6.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                .clickableNoRipple { if (!busy) onToggle() }
                .semantics { role = Role.Switch },
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
        ) {
            if (busy) {
                CircularProgressIndicator(Modifier.size(56.dp), color = Color.White, strokeWidth = 5.dp)
            } else {
                Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(8.dp))
                Text(if (online) "EN LIGNE" else "PASSER\nEN LIGNE", style = MaterialTheme.typography.headlineMedium,
                    color = Color.White, textAlign = TextAlign.Center)
                if (online) Text("toucher pour arrêter", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
            }
        }
    }
}
