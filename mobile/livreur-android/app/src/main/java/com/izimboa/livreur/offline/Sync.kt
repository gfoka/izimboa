package com.izimboa.livreur.offline

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.izimboa.livreur.App
import com.izimboa.livreur.data.ApiException
import com.izimboa.livreur.data.Delivery
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Envoie ce qui attend sur le téléphone, dans l'ordre : d'abord les positions, puis les actions.
 * Sans réseau, tout reste dans la base chiffrée et un travail Android (WorkManager) reprend l'envoi
 * dès que le réseau revient, même si l'application a été fermée ou le téléphone redémarré.
 */
class Sync(
    private val store: Queue,
    private val api: Remote,
    private val onDelivery: (Delivery) -> Unit,
    private val onRejected: (String) -> Unit,
    private val scheduleRetry: () -> Unit,
) {
    /** Ce que la synchronisation attend de la base locale (implémenté par [LocalStore], simulé dans les tests). */
    interface Queue {
        fun trimPositions(max: Int = 30_000)
        fun oldestPositions(limit: Int): List<LocalStore.StoredPoint>
        fun deletePositionsUpTo(id: Long)
        fun pendingPositions(): Long
        fun nextOutgoing(): LocalStore.Outgoing?
        fun done(id: Long)
        fun attempted(id: Long)
        fun pendingActions(): Long
    }

    /** Ce que la synchronisation attend du serveur (implémenté par [Api]). */
    interface Remote {
        suspend fun positions(points: List<com.izimboa.livreur.data.PointBody>)
        suspend fun goOffline()
        suspend fun step(id: String, action: String, body: String): Delivery
    }

    private val lock = Mutex()

    /** Retourne vrai si tout est parti. */
    suspend fun flush(): Boolean = lock.withLock {
        val ok = sendPositions() && sendActions()
        if (!ok && pending() > 0) scheduleRetry()
        ok
    }

    fun pending(): Long = store.pendingPositions() + store.pendingActions()

    private suspend fun sendPositions(): Boolean {
        store.trimPositions()
        while (true) {
            val batch = store.oldestPositions(200)
            if (batch.isEmpty()) return true
            try {
                api.positions(batch.map { it.point })
            } catch (e: ApiException) {
                if (e.status == 0 || e.status == 401 || e.status >= 500) return false
                // Refus définitif (ex. livreur hors ligne côté serveur) : ces points ne seront jamais acceptés.
            }
            store.deletePositionsUpTo(batch.last().id)
        }
    }

    private suspend fun sendActions(): Boolean {
        while (true) {
            val next = store.nextOutgoing() ?: return true
            try {
                when (next.kind) {
                    OFFLINE -> api.goOffline()
                    else -> onDelivery(api.step(next.deliveryId!!, next.kind, next.body))
                }
                store.done(next.id)
            } catch (e: ApiException) {
                if (e.status == 0 || e.status == 401 || e.status >= 500) {
                    store.attempted(next.id)
                    return false
                }
                // Action refusée par le serveur (déjà faite, course annulée…) : on la retire et on prévient.
                store.done(next.id)
                if (e.code != "invalid-transition") onRejected(e.message ?: "Action refusée.")
            }
        }
    }

    companion object {
        const val OFFLINE = "offline"
        private const val WORK = "izimboa-sync"

        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK, ExistingWorkPolicy.KEEP, request)
        }
    }
}

/** Lancé par Android quand le réseau revient. */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as App
        return if (app.sync.flush()) {
            app.courier.refreshDelivery()
            Result.success()
        } else Result.retry()
    }
}
