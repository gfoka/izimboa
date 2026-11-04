package com.izimboa.livreur

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.izimboa.livreur.ui.CourierFileScreen
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.izimboa.livreur.ui.DeliveryScreen
import com.izimboa.livreur.ui.HomeScreen
import com.izimboa.livreur.ui.IzimboaTheme
import com.izimboa.livreur.ui.LoginScreen
import com.izimboa.livreur.ui.NotCourierScreen
import com.izimboa.livreur.ui.OfferScreen
import kotlinx.coroutines.launch

/** Une seule activité ; l'écran affiché découle de l'état (connecté ? en ligne ? course proposée ? en cours ?). */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as App
        setContent { IzimboaTheme { Root(app) } }
    }

    /** Une course proposée s'affiche même téléphone verrouillé, écran rallumé. */
    fun showOverLockScreen(show: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(show)
            setTurnScreenOn(show)
        }
        if (show) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @Composable
    private fun Root(app: App) {
        val session by app.auth.session.collectAsStateWithLifecycle()
        val state by app.courier.state.collectAsStateWithLifecycle()
        val fileState by app.file.state.collectAsStateWithLifecycle()
        var showFile by rememberSaveable { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var loginMessage by remember { mutableStateOf<String?>(null) }

        val login = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            scope.launch { app.auth.completeLogin(result.data).onFailure { loginMessage = it.message } }
        }
        val logout = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
        val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            if (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true) scope.launch { app.courier.goOnline() }
            else loginMessage = "Sans la localisation, Izimboa ne peut pas vous proposer de courses près de vous."
        }

        // Retour dans l'application : on se recale sur le serveur.
        LifecycleResumeEffect(session) {
            if (session != null) scope.launch { app.courier.sync(); app.file.refresh(); app.registerPush() }
            onPauseOrDispose { }
        }
        LaunchedEffect(state.offer != null) { showOverLockScreen(state.offer != null) }

        val s = session
        val delivery = state.delivery
        val offer = state.offer
        val file = fileState.file
        val doLogout = { app.signOut()?.let { logout.launch(it) }; Unit }
        val fileActions = remember {
            com.izimboa.livreur.ui.FileActions(
                saveProfile = { f, l, e -> app.file.saveProfile(f, l, e) },
                upload = { code, number, expires, pages -> app.file.upload(code, number, expires, pages) },
                submit = { app.file.submit() },
                refresh = { app.file.refresh() },
                logout = { app.signOut()?.let { logout.launch(it) } },
                dismiss = { app.file.clearMessage() },
            )
        }
        val isCourier = s != null && "livreur" in s.roles
        when {
            s == null -> LoginScreen(onLogin = { loginMessage = null; login.launch(app.auth.loginIntent()) }, message = loginMessage)
            // Pas encore livreur : son dossier (identité, documents), puis l'attente de la validation (décision 74).
            !isCourier && file == null -> NotCourierScreen(
                name = s.name,
                phone = s.phone,
                onLogout = doLogout,
                onRetry = { scope.launch { app.file.refresh(); app.auth.reloadRoles() } },
                message = fileState.message,
            )
            !isCourier && file != null -> CourierFileScreen(file, fileState.busy, fileState.message, fileActions, onClose = null)
            // Livreur actif dont le dossier bloque (document expiré, délai dépassé) ou qui l'ouvre lui-même.
            delivery == null && offer == null && file != null && (!file.canWork || showFile) ->
                CourierFileScreen(file, fileState.busy, fileState.message, fileActions,
                    onClose = if (file.canWork) ({ showFile = false }) else null)
            delivery != null -> DeliveryScreen(
                d = delivery, busy = state.busy, message = state.message, info = state.info,
                pendingActions = state.pendingActions,
                onPickUp = { scope.launch { app.courier.pickUp() } },
                onDepart = { scope.launch { app.courier.depart() } },
                onDeliver = { code -> scope.launch { app.courier.deliver(code) } },
                onFail = { reason -> scope.launch { app.courier.fail(reason) } },
                onRetry = { scope.launch { app.courier.retry() } },
                onReturn = { scope.launch { app.courier.returnToSender() } },
                onClose = { app.courier.closeFinishedDelivery() },
                onDismiss = { app.courier.clearMessage() },
            )
            offer != null -> OfferScreen(
                offer = offer, receivedAt = state.offerReceivedAt, busy = state.busy,
                onAccept = { scope.launch { app.courier.accept() } },
                onDecline = { scope.launch { app.courier.decline() } },
                onExpired = { app.courier.offerExpired() },
            )
            else -> HomeScreen(
                name = s.name, online = state.online, busy = state.busy, message = state.message ?: loginMessage, info = state.info,
                gps = com.izimboa.livreur.ui.GpsStatus(state.locationEnabled, state.gpsAt, state.sentAt, state.sendError),
                onToggle = {
                    if (state.online) scope.launch { app.courier.goOffline() }
                    else {
                        val needed = buildList {
                            add(Manifest.permission.ACCESS_FINE_LOCATION)
                            add(Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        if (needed.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
                            scope.launch { app.courier.goOnline() }
                        } else permissions.launch(needed.toTypedArray())
                    }
                },
                onLogout = { app.signOut()?.let { logout.launch(it) } },
                onDismiss = { app.courier.clearMessage(); loginMessage = null },
                reminder = file?.let { f -> { com.izimboa.livreur.ui.FileReminder(f) { showFile = true } } },
            )
        }
    }
}
