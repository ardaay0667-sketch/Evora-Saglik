package com.evora.saglik.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.evora.saglik.MainActivity
import com.evora.saglik.R
import com.evora.saglik.data.PrefsManager
import com.evora.saglik.util.HealthCalculator

/**
 * Ana ekrana eklenebilen widget: günlük adım ve su durumunu gösterir.
 * Tamamen local veriden (PrefsManager) beslenir, internet gerekmez.
 */
class EvoraWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            requestUpdate(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.evora.saglik.WIDGET_REFRESH"

        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, EvoraWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val prefs = PrefsManager(context)
            prefs.resetIfNewDay(currentStepSensorValue = null)

            val steps = prefs.dailySteps
            val stepGoal = HealthCalculator.idealDailySteps(prefs.age, prefs.weightKg)
            val water = prefs.dailyWaterMl
            val waterGoal = HealthCalculator.dailyWaterTargetMl(prefs.age, prefs.weightKg)

            val views = RemoteViews(context.packageName, R.layout.widget_evora)
            views.setTextViewText(R.id.widget_steps_text, "$steps / $stepGoal adım")
            views.setTextViewText(R.id.widget_water_text, "$water / $waterGoal ml")
            views.setProgressBar(R.id.widget_steps_progress, stepGoal, steps.coerceAtMost(stepGoal), false)
            views.setProgressBar(R.id.widget_water_progress, waterGoal, water.coerceAtMost(waterGoal), false)

            val openAppIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context, 0, openAppIntent, android.app.PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
