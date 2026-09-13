package com.evora.saglik.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.evora.saglik.data.PrefsManager
import com.evora.saglik.widget.EvoraWidgetProvider
import java.util.Calendar

/**
 * Gece yarısı tetiklenir, günlük adım/su sayaçlarını sıfırlar.
 * StepCounterService içindeki resetIfNewDay() bir yedek güvenlik katmanı olarak
 * zaten her sensör olayında tarihi kontrol eder; bu receiver ise uygulama/servis
 * hiç açılmasa bile widget'ın doğru görünmesini sağlar.
 */
class DailyResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = PrefsManager(context)
        prefs.resetIfNewDay(currentStepSensorValue = null)
        EvoraWidgetProvider.requestUpdate(context)
        scheduleNext(context)
    }

    companion object {
        private const val REQUEST_CODE = 5001

        fun scheduleNext(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailyResetReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextMidnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 5)
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextMidnight.timeInMillis,
                pendingIntent
            )
        }
    }
}
