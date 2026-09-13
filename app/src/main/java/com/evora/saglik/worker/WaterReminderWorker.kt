package com.evora.saglik.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import com.evora.saglik.MainActivity
import com.evora.saglik.R
import com.evora.saglik.data.PrefsManager
import java.util.concurrent.TimeUnit

/**
 * Belirli aralıklarla (varsayılan 2 saat) su içmeyi hatırlatan bildirim gösterir.
 * Hedefe ulaşıldıysa hatırlatma göndermez.
 */
class WaterReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = PrefsManager(applicationContext)
        val target = com.evora.saglik.util.HealthCalculator.dailyWaterTargetMl(prefs.age, prefs.weightKg)

        if (prefs.dailyWaterMl < target) {
            showNotification()
        }
        return Result.success()
    }

    private fun showNotification() {
        val channelId = "evora_water_channel"
        val context = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                "Su Hatırlatma",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Günlük su hedefine ulaşman için hatırlatmalar"
            }
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("💧 Su içmeyi unutma!")
            .setContentText("Günlük su hedefine henüz ulaşmadın. Bir bardak su içmeye ne dersin?")
            .setSmallIcon(R.drawable.ic_water)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(2002, notification)
    }

    companion object {
        private const val WORK_NAME = "evora_water_reminder_work"

        /** Uygulama açılışında bir kez çağrılarak periyodik hatırlatmayı planlar */
        fun schedule(context: Context, intervalHours: Long = 2) {
            val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(
                intervalHours, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
