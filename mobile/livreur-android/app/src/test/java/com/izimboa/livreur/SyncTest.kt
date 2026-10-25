package com.izimboa.livreur

import com.izimboa.livreur.data.ApiException
import com.izimboa.livreur.data.Delivery
import com.izimboa.livreur.data.PointBody
import com.izimboa.livreur.data.Pricing
import com.izimboa.livreur.data.Stop
import com.izimboa.livreur.domain.Status
import com.izimboa.livreur.offline.LocalStore
import com.izimboa.livreur.offline.Sync
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private fun delivery(s: Status) = Delivery("d1", "IZ-1", s, Stop(4.0, 9.7, "A"), "+237", Stop(4.1, 9.8, "B"),
    "Awa", "+237", null, false, 1.0, Pricing(0, 1000, "PREPAID"))

/** Hors connexion : rien n'est perdu, tout part dans l'ordre, les refus du serveur ne bloquent pas la file. */
class SyncTest {

    private class FakeQueue : Sync.Queue {
        val positions = mutableListOf<LocalStore.StoredPoint>()
        val outbox = mutableListOf<LocalStore.Outgoing>()
        private var nextId = 1L
        fun point(i: Int) = positions.add(LocalStore.StoredPoint(nextId++, PointBody(4.0 + i / 1000.0, 9.7, recordedAt = "t$i")))
        fun action(kind: String) = outbox.add(LocalStore.Outgoing(nextId++, kind, if (kind == Sync.OFFLINE) null else "d1", "{}", 0))
        override fun trimPositions(max: Int) = Unit
        override fun oldestPositions(limit: Int) = positions.take(limit)
        override fun deletePositionsUpTo(id: Long) { positions.removeAll { it.id <= id } }
        override fun pendingPositions() = positions.size.toLong()
        override fun nextOutgoing() = outbox.firstOrNull()
        override fun done(id: Long) { outbox.removeAll { it.id == id } }
        override fun attempted(id: Long) = Unit
        override fun pendingActions() = outbox.size.toLong()
    }

    private class FakeRemote : Sync.Remote {
        var networkUp = true
        val received = mutableListOf<String>()
        var reject: String? = null
        override suspend fun positions(points: List<PointBody>) {
            if (!networkUp) throw ApiException(0, "network", "x")
            received += "positions:${points.size}"
        }
        override suspend fun goOffline() {
            if (!networkUp) throw ApiException(0, "network", "x")
            received += "offline"
        }
        override suspend fun step(id: String, action: String, body: String): Delivery {
            if (!networkUp) throw ApiException(0, "network", "x")
            if (action == reject) throw ApiException(422, "invalid-request", "Refusé")
            received += action
            return delivery(if (action == "pickup") Status.PICKED_UP else Status.IN_TRANSIT)
        }
    }

    @Test fun sansReseauToutResteEtRepartDansLOrdre() = runBlocking {
        val q = FakeQueue(); val r = FakeRemote(); var retries = 0
        val sync = Sync(q, r, {}, {}, { retries++ })
        repeat(450) { q.point(it) }
        q.action("pickup"); q.action("depart"); q.action(Sync.OFFLINE)

        r.networkUp = false
        assertFalse(sync.flush())
        assertEquals(450L + 3, sync.pending())        // rien de perdu
        assertEquals(1, retries)                      // reprise programmée au retour du réseau

        r.networkUp = true
        assertTrue(sync.flush())
        assertEquals(0L, sync.pending())
        // Positions d'abord (par paquets de 200), puis les actions dans l'ordre, « hors ligne » en dernier.
        assertEquals(listOf("positions:200", "positions:200", "positions:50", "pickup", "depart", "offline"), r.received)
    }

    @Test fun unRefusDuServeurNeBloquePasLaFile() = runBlocking {
        val q = FakeQueue(); val r = FakeRemote().apply { reject = "pickup" }
        val messages = mutableListOf<String>(); val updates = mutableListOf<Status>()
        val sync = Sync(q, r, { updates += it.status }, { messages += it }, {})
        q.action("pickup"); q.action("depart")
        assertTrue(sync.flush())
        assertEquals(listOf("depart"), r.received)
        assertEquals(listOf("Refusé"), messages)
        assertEquals(listOf(Status.IN_TRANSIT), updates)
    }
}
