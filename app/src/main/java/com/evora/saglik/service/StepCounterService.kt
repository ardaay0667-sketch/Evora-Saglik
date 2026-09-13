package com.evora.saglik.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.evora.saglik.MainActivity
import com.evora.saglik.R
import com.evora.saglik.data.PrefsManager
import com.evora.saglik.widget.EvoraWidgetProvider

/**
 * TYPE_STEP_COUNTER sensörünü dinleyen foreground servis.
 * Uygulama kapalıyken de adım saymaya devam eder (internet gerekmez).
 * Sensör, cihaz açıldığından beri toplam adımı verir; biz bunu bir "baseline"
 * değerinden çıkararak günlük adımı hesaplarız.
 */
class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var prefsManager: PrefsManager

    override fun onCreate() {
        super.onCreate()
        prefsManager = PrefsManager(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        startForeground(NOTIFICATION_ID, buildNotification())

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Sistem tarafından öldürülürse tekrar başlatılsın
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        val totalStepsSinceBoot = event.values[0]

        // Gün değiştiyse sıfırla (yeni baseline = şu anki sensör değeri)
        prefsManager.resetIfNewDay(totalStepsSinceBoot)

        // İlk okuma ise baseline'ı ayarla
        if (prefsManager.stepBaseline < 0f) {
            prefsManager.stepBaseline = totalStepsSinceBoot
        }

        val daily = (totalStepsSinceBoot - prefsManager.stepBaseline).toInt().coerceAtLeast(0)
        prefsManager.dailySteps = daily

        // Widget'ı güncelle
        EvoraWidgetProvider.requestUpdate(applicationContext)

        // Ana ekran açıksa canlı güncelleme için broadcast gönder
        sendBroadcast(Intent(ACTION_STEPS_UPDATED).setPackage(packageName))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Kullanılmıyor
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    private fun buildNotification(): android.app.Notification {
        val channelId = "evora_step_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                "Adım Sayar",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Evora Sağlık adım sayımını arka planda sürdürür"
            }
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, openAppIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Evora Sağlık")
            .setContentText("Adımların sayılıyor...")
            .setSmallIcon(R.drawable.ic_steps)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STEPS_UPDATED = "com.evora.saglik.STEPS_UPDATED"
    }
}
