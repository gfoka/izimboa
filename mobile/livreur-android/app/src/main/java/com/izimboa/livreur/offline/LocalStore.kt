package com.izimboa.livreur.offline

import android.content.ContentValues
import android.content.Context
import com.izimboa.livreur.auth.SecureStore
import com.izimboa.livreur.data.PointBody
import java.security.SecureRandom
import java.util.Base64
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

/**
 * Base locale chiffrée (SQLCipher, AES-256) : rien n'est perdu sans réseau, même si le téléphone s'éteint.
 * La clé est aléatoire, propre à ce téléphone, et rangée chiffrée par le Keystore Android :
 * un fichier copié (câble, téléphone « rooté ») est illisible (décision n° 62).
 *
 * - positions : points GPS pas encore envoyés ;
 * - outbox    : actions du livreur (ramassé, parti, échec…) à envoyer, dans l'ordre ;
 * - state     : dernière course connue, pour l'afficher même hors réseau après un redémarrage.
 */
class LocalStore(context: Context) : SQLiteOpenHelper(
    context.applicationContext, "izimboa-livreur.db", key(context), null, VERSION, 0, null, null, true,
), Sync.Queue {
    data class Outgoing(val id: Long, val kind: String, val deliveryId: String?, val body: String, val attempts: Int)
    data class StoredPoint(val id: Long, val point: PointBody)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE positions (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              lat REAL NOT NULL, lng REAL NOT NULL, accuracy REAL, speed REAL, heading REAL,
              recorded_at TEXT NOT NULL)""")
        db.execSQL("""
            CREATE TABLE outbox (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              kind TEXT NOT NULL, delivery_id TEXT, body TEXT NOT NULL,
              created_at INTEGER NOT NULL, attempts INTEGER NOT NULL DEFAULT 0)""")
        db.execSQL("CREATE TABLE state (name TEXT PRIMARY KEY, value TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    // ---- Positions ----

    fun addPosition(p: PointBody) {
        writableDatabase.insert("positions", null, ContentValues().apply {
            put("lat", p.lat); put("lng", p.lng)
            p.accuracy?.let { put("accuracy", it) }; p.speed?.let { put("speed", it) }; p.heading?.let { put("heading", it) }
            put("recorded_at", p.recordedAt)
        })
    }

    override fun oldestPositions(limit: Int): List<StoredPoint> =
        readableDatabase.rawQuery("SELECT id, lat, lng, accuracy, speed, heading, recorded_at FROM positions ORDER BY id LIMIT $limit", null).use { c ->
            buildList {
                while (c.moveToNext()) add(StoredPoint(c.getLong(0), PointBody(
                    lat = c.getDouble(1), lng = c.getDouble(2),
                    accuracy = if (c.isNull(3)) null else c.getFloat(3),
                    speed = if (c.isNull(4)) null else c.getFloat(4),
                    heading = if (c.isNull(5)) null else c.getFloat(5),
                    recordedAt = c.getString(6),
                )))
            }
        }

    override fun deletePositionsUpTo(id: Long) { writableDatabase.delete("positions", "id <= ?", arrayOf(id.toString())) }

    override fun pendingPositions(): Long = count("positions")

    /** Garde au plus ~2 jours de points (le serveur refuse au-delà de 24 h) : le téléphone ne se remplit jamais. */
    override fun trimPositions(max: Int) {
        writableDatabase.execSQL("DELETE FROM positions WHERE id NOT IN (SELECT id FROM positions ORDER BY id DESC LIMIT $max)")
    }

    // ---- Actions à envoyer ----

    fun enqueue(kind: String, deliveryId: String?, body: String) {
        writableDatabase.insert("outbox", null, ContentValues().apply {
            put("kind", kind); put("delivery_id", deliveryId); put("body", body); put("created_at", System.currentTimeMillis())
        })
    }

    override fun nextOutgoing(): Outgoing? =
        readableDatabase.rawQuery("SELECT id, kind, delivery_id, body, attempts FROM outbox ORDER BY id LIMIT 1", null).use { c ->
            if (c.moveToFirst()) Outgoing(c.getLong(0), c.getString(1), if (c.isNull(2)) null else c.getString(2), c.getString(3), c.getInt(4)) else null
        }

    override fun done(id: Long) { writableDatabase.delete("outbox", "id = ?", arrayOf(id.toString())) }

    override fun attempted(id: Long) { writableDatabase.execSQL("UPDATE outbox SET attempts = attempts + 1 WHERE id = ?", arrayOf<Any>(id)) }

    override fun pendingActions(): Long = count("outbox")

    // ---- État ----

    fun put(name: String, value: String?) {
        if (value == null) writableDatabase.delete("state", "name = ?", arrayOf(name))
        else writableDatabase.insertWithOnConflict("state", null, ContentValues().apply { put("name", name); put("value", value) }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun get(name: String): String? =
        readableDatabase.rawQuery("SELECT value FROM state WHERE name = ?", arrayOf(name)).use { c -> if (c.moveToFirst()) c.getString(0) else null }

    /** Déconnexion : on efface tout ce qui concerne le livreur. */
    fun clear() {
        writableDatabase.apply { delete("positions", null, null); delete("outbox", null, null); delete("state", null, null) }
    }

    private fun count(table: String): Long =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM $table", null).use { c -> if (c.moveToFirst()) c.getLong(0) else 0 }

    companion object {
        private const val VERSION = 1

        init { System.loadLibrary("sqlcipher") }

        private fun key(context: Context): ByteArray {
            val secure = SecureStore(context)
            secure.get("db_key")?.let { return Base64.getDecoder().decode(it) }
            val key = ByteArray(32).also { SecureRandom().nextBytes(it) }
            secure.put("db_key", Base64.getEncoder().encodeToString(key))
            return key
        }
    }
}
