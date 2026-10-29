package com.izimboa.livreur.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.izimboa.livreur.BuildConfig
import com.izimboa.livreur.domain.realmRoles
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.ResponseTypeValues

/**
 * Connexion Izimboa (Keycloak, auth.izimboa.com) par le navigateur du téléphone, flux « code + PKCE ».
 * Session longue (scope offline_access) : le livreur ne se reconnecte pas chaque jour (décision n° 40).
 */
class Auth(context: Context) {

    /** [phone] : numéro vérifié par code WhatsApp (+237…), absent pour une connexion par e-mail. */
    data class Session(val name: String?, val roles: Set<String>, val phone: String? = null)

    private val store = SecureStore(context)
    private val service = AuthorizationService(context.applicationContext)
    private val lock = Mutex()
    private var state: AuthState = store.get(KEY)?.let { runCatching { AuthState.jsonDeserialize(it) }.getOrNull() } ?: AuthState()

    private val _session = MutableStateFlow(sessionOf(state))
    val session: StateFlow<Session?> = _session

    private val config = AuthorizationServiceConfiguration(
        Uri.parse("${BuildConfig.AUTH_URL}/protocol/openid-connect/auth"),
        Uri.parse("${BuildConfig.AUTH_URL}/protocol/openid-connect/token"),
        null,
        Uri.parse("${BuildConfig.AUTH_URL}/protocol/openid-connect/logout"),
    )

    /** Intention à lancer pour ouvrir la page de connexion Izimboa. */
    fun loginIntent(): Intent {
        val request = AuthorizationRequest.Builder(config, BuildConfig.CLIENT_ID, ResponseTypeValues.CODE, Uri.parse(BuildConfig.REDIRECT_URI))
            .setScopes("openid", "profile", "email", "phone", "offline_access")
            .setUiLocales("fr")
            .build()
        return service.getAuthorizationRequestIntent(request)
    }

    /** Retour de la page de connexion : échange du code contre les jetons. */
    suspend fun completeLogin(data: Intent?): Result<Unit> {
        val response = data?.let { AuthorizationResponse.fromIntent(it) }
        val error = data?.let { AuthorizationException.fromIntent(it) }
        if (response == null) return Result.failure(IllegalStateException(error?.errorDescription ?: "Connexion annulée."))
        return suspendCancellableCoroutine { cont ->
            service.performTokenRequest(response.createTokenExchangeRequest()) { tokens, ex ->
                state = AuthState(response, tokens, ex)
                save()
                cont.resume(if (tokens != null) Result.success(Unit) else Result.failure(IllegalStateException(ex?.errorDescription ?: "Connexion impossible.")))
            }
        }
    }

    /** Jeton d'accès valide (renouvelé si besoin), ou null si la session est perdue. */
    suspend fun accessToken(): String? = lock.withLock {
        if (!state.isAuthorized) return null
        suspendCancellableCoroutine { cont ->
            state.performActionWithFreshTokens(service) { token, _, ex ->
                if (ex != null && ex.type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR) {
                    state = AuthState() // jeton de renouvellement refusé : reconnexion nécessaire
                }
                save()
                cont.resume(token)
            }
        }
    }

    /** Force un nouveau jeton (ex. le rôle « livreur » vient d'être donné dans Keycloak). */
    suspend fun reloadRoles() {
        lock.withLock { state.needsTokenRefresh = true }
        accessToken()
    }

    fun logoutIntent(): Intent? {
        val idToken = state.idToken
        state = AuthState()
        save()
        if (idToken == null) return null
        val request = EndSessionRequest.Builder(config)
            .setIdTokenHint(idToken)
            .setPostLogoutRedirectUri(Uri.parse(BuildConfig.REDIRECT_URI))
            .build()
        return service.getEndSessionRequestIntent(request)
    }

    private fun save() {
        store.put(KEY, if (state.isAuthorized) state.jsonSerializeString() else null)
        _session.value = sessionOf(state)
    }

    private fun sessionOf(s: AuthState): Session? {
        if (!s.isAuthorized) return null
        val claims = s.parsedIdToken?.additionalClaims
        val name = (claims?.get("given_name") ?: claims?.get("name")) as? String
        val phone = (claims?.get("phone_number") as? String).takeIf { claims?.get("phone_number_verified") == true }
        return Session(name, realmRoles(s.accessToken), phone)
    }

    private companion object { const val KEY = "auth_state" }
}
