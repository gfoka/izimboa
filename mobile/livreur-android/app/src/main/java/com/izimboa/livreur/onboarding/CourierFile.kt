package com.izimboa.livreur.onboarding

import com.izimboa.livreur.auth.Auth
import com.izimboa.livreur.data.Api
import com.izimboa.livreur.data.ApiException
import com.izimboa.livreur.data.MyFile
import com.izimboa.livreur.data.ProfileBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Dossier du livreur (décision 74) : identité, documents photographiés, décision de l'équipe Izimboa.
 * Une fois le dossier validé, le jeton est renouvelé pour obtenir le rôle « livreur ».
 */
class CourierFile(private val api: Api, private val auth: Auth) {

    data class State(val file: MyFile? = null, val loaded: Boolean = false, val busy: Boolean = false, val message: String? = null)

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    suspend fun refresh() = run(silent = true) { api.myFile() }

    suspend fun saveProfile(first: String, last: String, email: String?) =
        run { api.saveProfile(ProfileBody(first.trim(), last.trim(), email?.trim()?.ifBlank { null })) }

    suspend fun upload(code: String, number: String?, expiresOn: String?, pages: Map<Int, ByteArray>) =
        run { api.uploadDocument(code, number, expiresOn, pages) }

    suspend fun submit() = run { api.submitFile() }

    fun clearMessage() = _state.update { it.copy(message = null) }

    fun reset() { _state.value = State() }

    /** Renvoie true si l'action a réussi. */
    private suspend fun run(silent: Boolean = false, action: suspend () -> MyFile): Boolean {
        _state.update { it.copy(busy = !silent, message = null) }
        return try {
            val file = action()
            _state.update { it.copy(file = file, loaded = true, busy = false) }
            if (file.status == "ACTIVE" && "livreur" !in (auth.session.value?.roles ?: emptySet())) auth.reloadRoles()
            if (file.status != "ACTIVE" && "livreur" in (auth.session.value?.roles ?: emptySet())) auth.reloadRoles()
            true
        } catch (e: ApiException) {
            _state.update { it.copy(busy = false, loaded = it.loaded || silent, message = e.message) }
            false
        }
    }
}
