package com.izimboa.livreur.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.izimboa.livreur.MainActivity
import com.izimboa.livreur.R
import com.izimboa.livreur.data.Offer
import com.izimboa.livreur.domain.fcfa

/** Notifications : « vous êtes en ligne » (permanente) et « nouvelle course » (sonne comme un appel). */
object Notifications {

    const val CHANNEL_ONLINE = "online"
    const val CHANNEL_OFFER = "offers"
    const val ID_ONLINE = 1
    const val ID_OFFER = 2

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ONLINE, "En ligne", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Affichée tant que vous êtes en ligne et que votre position est partagée."
                setShowBadge(false)
            },
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_OFFER, "Nouvelles courses", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Sonne quand une course vous est proposée."
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build(),
                )
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 400, 600, 400, 600)
            },
        )
    }

    fun online(context: Context): Notification =
        NotificationCompat.Builder(context, CHANNEL_ONLINE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Vous êtes en ligne")
            .setContentText("Votre position est partagée pour recevoir des courses.")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(openApp(context))
            .build()

    /** Plein écran si le téléphone est verrouillé : le livreur voit la course tout de suite. */
    fun offer(context: Context, offer: Offer) {
        val n = NotificationCompat.Builder(context, CHANNEL_OFFER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Nouvelle course : ${fcfa(offer.courierEarning)}")
            .setContentText("Ramassage : ${offer.delivery.pickupAddress}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setTimeoutAfter(offer.secondsLeft * 1000L)
            .setContentIntent(openApp(context))
            .setFullScreenIntent(openApp(context), true)
            .build()
        runCatching { context.getSystemService(NotificationManager::class.java).notify(ID_OFFER, n) }
    }

    fun cancelOffer(context: Context) = context.getSystemService(NotificationManager::class.java).cancel(ID_OFFER)

    private fun openApp(context: Context): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
